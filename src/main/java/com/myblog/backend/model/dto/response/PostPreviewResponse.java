package com.myblog.backend.model.dto.response;

import java.util.List;

public record PostPreviewResponse(
        Long id,
        String title,
        String text,
        List<String> tags,
        Integer likesCount,
        Integer commentsCount
) {}
