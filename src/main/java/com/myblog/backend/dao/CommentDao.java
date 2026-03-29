package com.myblog.backend.dao;

import com.myblog.backend.model.dto.response.CommentResponse;

import java.util.List;

public interface CommentDao {
    List<CommentResponse> findByPostId(long postId);
    java.util.Optional<CommentResponse> findById(long postId, long commentId);
    long save(long postId, String text);
    boolean deleteById(long postId, long commentId);
    boolean updateById(long postId, long commentId, String text);
}
