package com.myblog.backend.service;
import com.myblog.backend.model.dto.response.PostsPageResponse;

public interface PostService {
    PostsPageResponse getPosts(String search, int pageNumber, int pageSize);
}
