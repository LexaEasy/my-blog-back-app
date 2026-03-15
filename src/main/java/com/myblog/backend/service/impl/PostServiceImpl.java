package com.myblog.backend.service.impl;

import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
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
                        p.getTags(),
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
        List<String> tags = normalizeTags(request.getTags());

        long id = postDao.save(title, text, tags);
        Post created = postDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("Созданный пост не найден"));

        return new PostPreviewResponse(
                created.getId(),
                created.getTitle(),
                created.getText(),
                created.getTags(),
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
    public Optional<PostPreviewResponse> updatePost(long id, UpdatePostRequest request) {
        Optional<Post> existingOpt = postDao.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }

        Post existing = existingOpt.get();

        UpdatePostRequest normalized = new UpdatePostRequest(
                request.getTitle(),
                request.getText(),
                existing.getLikesCount(),
                existing.getCommentsCount(),
                normalizeTags(request.getTags())
        );

        boolean updated = postDao.updateById(id, normalized);
        if (!updated) {
            return Optional.empty();
        }

        return postDao.findById(id)
                .map(p -> new PostPreviewResponse(
                        p.getId(),
                        p.getTitle(),
                        p.getText(),
                        p.getTags(),
                        p.getLikesCount(),
                        p.getCommentsCount()
                ));
    }

    @Override
    public boolean exists(long id) {
        return postDao.findById(id).isPresent();
    }

    @Override
    public byte[] getImage(long id) {
        Optional<byte[]> storedImage = postDao.findImageById(id);
        if (storedImage.isPresent()) {
            return storedImage.get();
        }
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
                        p.getTags(),
                        p.getLikesCount(),
                        p.getCommentsCount()
                ));
    }

    @Override
    @Transactional
    public int addLike(long postId, String userKey) {
        ensurePostExists(postId);
        postDao.addLike(postId, userKey);
        return postDao.getLikesCount(postId);
    }

    @Override
    @Transactional
    public int removeLike(long postId, String userKey) {
        ensurePostExists(postId);
        postDao.removeLike(postId, userKey);
        return postDao.getLikesCount(postId);
    }

    @Override
    public int getLikesCount(long postId) {
        ensurePostExists(postId);
        return postDao.getLikesCount(postId);
    }

    @Override
    @Transactional
    public boolean updateImage(long id, MultipartFile image) {
        if (postDao.findById(id).isEmpty()) {
            return false;
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("empty file");
        }

        try {
            return postDao.updateImageById(id, image.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать загруженную картинку", e);
        }
    }

    private void ensurePostExists(long postId) {
        if (postDao.findById(postId).isEmpty()) {
            throw new IllegalArgumentException("Пост с id=" + postId + " не найден");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
    }
}
