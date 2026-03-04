package com.myblog.backend.service;

import com.myblog.backend.model.dto.response.CommentResponse;

import java.util.List;

public interface CommentService {
    List<CommentResponse> getByPostId(long postId);
}