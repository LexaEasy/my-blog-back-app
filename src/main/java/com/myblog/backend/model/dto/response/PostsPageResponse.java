package com.myblog.backend.model.dto.response;

import java.util.List;

public record PostsPageResponse(
        List<PostPreviewResponse> posts,
        boolean hasNext,
        boolean hasPrev,
        int lastPage
) {}
