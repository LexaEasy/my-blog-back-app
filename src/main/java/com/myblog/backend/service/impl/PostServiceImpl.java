package com.myblog.backend.service.impl;

import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import java.util.List;
import org.springframework.stereotype.Service;
import com.myblog.backend.dao.PostDao;

@Service
public class PostServiceImpl implements PostService {
    private final PostDao postDao;

    public PostServiceImpl(PostDao postDao) {
        this.postDao = postDao;
    }

    @Override
    public PostsPageResponse getPosts(String search, int pageNumber, int pageSize) {
        List<Post> posts = postDao.findAll(search, pageNumber, pageSize);
        int total = postDao.count(search);

        int lastPage = (int) Math.ceil((double) total / pageSize);
        if (lastPage == 0) lastPage = 1;

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        List<PostPreviewResponse> result = posts.stream()
                .map(p -> new PostPreviewResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getText(),
                        List.of(), // tags позже
                        p.getLikesCount(),
                        p.getCommentsCount()
                ))
                .toList();

        return new PostsPageResponse(result, hasNext, hasPrev, lastPage);
    }
}
