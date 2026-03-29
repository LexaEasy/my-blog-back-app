package com.myblog.backend.service;

import com.myblog.backend.model.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    List<CommentResponse> getByPostId(long postId);
    java.util.Optional<CommentResponse> getById(long postId, long commentId);
    CommentResponse add(long postId, String text);
    boolean delete(long postId, long commentId);
    CommentResponse update(long postId, long commentId, String text);
}
