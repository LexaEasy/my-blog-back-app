package com.myblog.backend.controller;

import com.myblog.backend.model.dto.response.PostsPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.myblog.backend.service.PostService;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

     @GetMapping
        public PostsPageResponse getPosts(
             @RequestParam(defaultValue = "") String search,
             @RequestParam(defaultValue = "1") int pageNumber,
             @RequestParam(defaultValue = "5") int pageSize
     )
     {

         return postService.getPosts(search, pageNumber, pageSize);
    }
}
