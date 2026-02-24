package com.myblog.backend.controller;

import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
 @GetMapping
    public PostsPageResponse getPosts(
         @RequestParam(defaultValue = "") String search,
         @RequestParam(defaultValue = "1") int pageNumber,
         @RequestParam(defaultValue = "5") int pageSize
 ) {
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
