package com.myblog.backend.service;

import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;

import java.util.Optional;

public interface PostService {
    PostsPageResponse getPosts(String search, int pageNumber, int pageSize);
    PostPreviewResponse createPost(CreatePostRequest request);
    void deletePost(long id);
    boolean updatePost(long id, UpdatePostRequest request);
    boolean exists(long id);
    byte[] getImage(long id);
    Optional<PostPreviewResponse> getPostById(long id);

    int addLike(long postId, String userKey);
    int removeLike(long postId, String userKey);
    int getLikesCount(long postId);
}
