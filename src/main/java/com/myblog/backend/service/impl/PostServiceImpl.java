package com.myblog.backend.service.impl;

import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

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
                        List.of(),
                        p.getLikesCount(),
                        p.getCommentsCount()
                ))
                .toList();

        return new PostsPageResponse(result, hasNext, hasPrev, lastPage);
    }

    @Override
    public PostPreviewResponse createPost(CreatePostRequest request) {
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        String text = request.getText() == null ? "" : request.getText().trim();

        if (title.isBlank()) {
            throw new IllegalArgumentException("title не должен быть пустым");
        }
        if (text.isBlank()) {
            throw new IllegalArgumentException("text не должен быть пустым");
        }

        long id = postDao.save(title, text);
        Post created = postDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("Созданный пост не найден"));

        return new PostPreviewResponse(
                created.getId(),
                created.getTitle(),
                created.getText(),
                List.of(),
                created.getLikesCount(),
                created.getCommentsCount()
        );
    }

    @Override
    public void deletePost(long id) {
        boolean deleted = postDao.deleteById(id);
        if (!deleted) {
            throw new IllegalArgumentException("Пост с id=" + id + " не найден");
        }
    }

    @Override
    public boolean updatePost(long id, UpdatePostRequest request) {
        return postDao.updateById(id, request);
    }

    @Override
    public boolean exists(long id) {
        return postDao.findById(id).isPresent();
    }

    @Override
    public byte[] getImage(long id) {
        ClassPathResource resource = new ClassPathResource("static/images/default-post.png");
        try (InputStream in = resource.getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать default-post.png", e);
        }
    }

    @Override
    public Optional<PostPreviewResponse> getPostById(long id) {
        return postDao.findById(id)
                .map(p -> new PostPreviewResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getText(),
                        List.of(),
                        p.getLikesCount(),
                        p.getCommentsCount()
                ));
    }
}
