package com.devtrackpro.controller;

import com.devtrackpro.dto.CommentRequest;
import com.devtrackpro.dto.CommentResponse;
import com.devtrackpro.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Comments", description = "Endpoints for task comments, threaded nested replies, and mentions")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/tasks/{taskId}/comments")
    @PreAuthorize("@security.isTaskMember(#taskId)")
    @Operation(summary = "Post a comment on a task (supports parentCommentId for threaded replies)")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long taskId,
                                                         @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(taskId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    @Operation(summary = "Update comment content (Only creator or organization OWNER/ADMIN)")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id,
                                                         @Valid @RequestBody CommentRequest request,
                                                         Principal principal) {
        CommentResponse response = commentService.updateComment(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete comment (Only creator or organization OWNER/ADMIN)")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, Principal principal) {
        commentService.deleteComment(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tasks/{taskId}/comments")
    @PreAuthorize("@security.isTaskMember(#taskId)")
    @Operation(summary = "Get comments tree on a task")
    public ResponseEntity<List<CommentResponse>> getThreadedComments(@PathVariable Long taskId) {
        List<CommentResponse> response = commentService.getThreadedComments(taskId);
        return ResponseEntity.ok(response);
    }
}
