package com.devtrackpro.service.impl;

import com.devtrackpro.dto.CommentRequest;
import com.devtrackpro.dto.CommentResponse;
import com.devtrackpro.dto.UserSummaryResponse;
import com.devtrackpro.entity.*;
import com.devtrackpro.exception.BadRequestException;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.CommentRepository;
import com.devtrackpro.repository.OrganizationMemberRepository;
import com.devtrackpro.repository.TaskRepository;
import com.devtrackpro.repository.UserRepository;
import com.devtrackpro.service.CommentService;
import com.devtrackpro.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final OrganizationMemberRepository memberRepository;
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              TaskRepository taskRepository,
                              UserRepository userRepository,
                              OrganizationMemberRepository memberRepository,
                              NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.notificationService = notificationService;
    }

    @Override
    public CommentResponse createComment(Long taskId, CommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        User author = getAuthenticatedUser();
        Comment parent = null;
        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .task(task)
                .author(author)
                .parentComment(parent)
                .content(request.getContent())
                .build();

        // Parse @mentions (e.g. @alice)
        Set<User> mentions = new HashSet<>();
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9_-]+)");
        Matcher matcher = pattern.matcher(request.getContent());
        while (matcher.find()) {
            String username = matcher.group(1);
            userRepository.findByUsername(username).ifPresent(mentionedUser -> {
                // Ensure mentioned user belongs to the organization
                Long orgId = task.getProject().getWorkspace().getOrganization().getId();
                if (memberRepository.existsByOrganizationIdAndUserId(orgId, mentionedUser.getId())) {
                    mentions.add(mentionedUser);
                    
                    // Notify mentioned user
                    notificationService.notifyUser(
                            mentionedUser.getId(),
                            "MENTION",
                            author.getFirstName() + " " + author.getLastName() + " mentioned you in a comment on " + task.getTaskKey(),
                            task.getId()
                    );
                }
            });
        }
        comment.setMentions(mentions);

        Comment saved = commentRepository.save(comment);

        // Notify Task Assignee (if not the commenter itself)
        if (task.getAssignee() != null && !task.getAssignee().getId().equals(author.getId())) {
            notificationService.notifyUser(
                    task.getAssignee().getId(),
                    "COMMENT_ADDED",
                    author.getFirstName() + " " + author.getLastName() + " commented on your task " + task.getTaskKey(),
                    task.getId()
            );
        }

        return mapToResponse(saved);
    }

    @Override
    public CommentResponse updateComment(Long id, CommentRequest request, String currentUserEmail) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        validateCommentPermissions(comment, currentUserEmail);

        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    @Override
    public void deleteComment(Long id, String currentUserEmail) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        validateCommentPermissions(comment, currentUserEmail);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getThreadedComments(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found");
        }

        List<Comment> comments = commentRepository.findByTaskId(taskId);
        
        // 1. Map all comments to responses (initial empty replies list)
        List<CommentResponse> responses = comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // 2. Put in map for fast lookup
        Map<Long, CommentResponse> responseMap = responses.stream()
                .collect(Collectors.toMap(CommentResponse::getId, r -> r));

        List<CommentResponse> rootComments = new ArrayList<>();

        // 3. Thread the comments
        for (CommentResponse r : responses) {
            if (r.getParentCommentId() == null) {
                rootComments.add(r);
            } else {
                CommentResponse parent = responseMap.get(r.getParentCommentId());
                if (parent != null) {
                    if (parent.getReplies() == null) {
                        parent.setReplies(new ArrayList<>());
                    }
                    parent.getReplies().add(r);
                }
            }
        }

        // Sort comments by ID (chronological)
        rootComments.sort(Comparator.comparing(CommentResponse::getId));
        for (CommentResponse root : rootComments) {
            sortReplies(root);
        }

        return rootComments;
    }

    private void sortReplies(CommentResponse c) {
        if (c.getReplies() != null) {
            c.getReplies().sort(Comparator.comparing(CommentResponse::getId));
            for (CommentResponse child : c.getReplies()) {
                sortReplies(child);
            }
        }
    }

    private void validateCommentPermissions(Comment comment, String email) {
        // Author is allowed
        if (comment.getAuthor().getEmail().equalsIgnoreCase(email)) {
            return;
        }

        // Check if user is OWNER or ADMIN in organization
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Long orgId = comment.getTask().getProject().getWorkspace().getOrganization().getId();
        Optional<OrganizationMember> memberOpt = memberRepository.findByOrganizationIdAndUserId(orgId, user.getId());
        if (memberOpt.isPresent()) {
            OrganizationRole role = memberOpt.get().getRole();
            if (role == OrganizationRole.OWNER || role == OrganizationRole.ADMIN) {
                return;
            }
        }

        throw new AccessDeniedException("You do not have permission to modify or delete this comment");
    }

    private CommentResponse mapToResponse(Comment c) {
        UserSummaryResponse author = UserSummaryResponse.builder()
                .id(c.getAuthor().getId())
                .username(c.getAuthor().getUsername())
                .email(c.getAuthor().getEmail())
                .firstName(c.getAuthor().getFirstName())
                .lastName(c.getAuthor().getLastName())
                .avatarUrl(c.getAuthor().getAvatarUrl())
                .build();

        List<UserSummaryResponse> mentions = c.getMentions().stream()
                .map(m -> UserSummaryResponse.builder()
                        .id(m.getId())
                        .username(m.getUsername())
                        .email(m.getEmail())
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .avatarUrl(m.getAvatarUrl())
                        .build())
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(c.getId())
                .taskId(c.getTask().getId())
                .author(author)
                .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                .content(c.getContent())
                .mentions(mentions)
                .replies(new ArrayList<>())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.authentication.BadCredentialsException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + auth.getName()));
    }
}
