package com.devtrackpro.service;

import com.devtrackpro.dto.CommentRequest;
import com.devtrackpro.dto.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long taskId, CommentRequest request);
    CommentResponse updateComment(Long id, CommentRequest request, String currentUserEmail);
    void deleteComment(Long id, String currentUserEmail);
    List<CommentResponse> getThreadedComments(Long taskId);
}
