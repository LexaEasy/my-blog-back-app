package com.myblog.backend.model.dto.response;

public record CommentResponse(
        Long id,
        String text,
        Long postId
) {
}
