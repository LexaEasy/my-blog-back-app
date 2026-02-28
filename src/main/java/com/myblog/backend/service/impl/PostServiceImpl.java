package com.myblog.backend.service.impl;

import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {

    @Override
    public PostsPageResponse getPosts(String search, int pageNumber, int pageSize) {
        // текущая мок-логика из контроллера
        List<PostPreviewResponse> mockPosts = List.of(
                new PostPreviewResponse(
                        1L,
                        "Первый пост",
                        "для проверки фронта.",
                        List.of("java", "spring"),
                        12,
                        3
                ),
                new PostPreviewResponse(
                        2L,
                        "Второй пост",
                        "для проверки фронта.",
                        List.of("backend"),
                        5,
                        1
                )
        );

        return new PostsPageResponse(
                mockPosts,
                false,
                pageNumber > 1, //предыдущая страница теоретически есть.
                1
        );
    }
}
