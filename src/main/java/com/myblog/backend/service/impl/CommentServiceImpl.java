package com.myblog.backend.service.impl;

import com.myblog.backend.dao.CommentDao;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.dto.response.CommentResponse;
import com.myblog.backend.service.CommentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;
    private final PostDao postDao;

    public CommentServiceImpl(CommentDao commentDao, PostDao postDao) {
        this.commentDao = commentDao;
        this.postDao = postDao;
    }

    @Override
    public List<CommentResponse> getByPostId(long postId) {
        if (postDao.findById(postId).isEmpty()) {
            return List.of();
        }
        return commentDao.findByPostId(postId);
    }

    @Override
    @Transactional
    public CommentResponse add(long postId, String text) {
        if (postDao.findById(postId).isEmpty()) {
            throw new IllegalArgumentException("Пост с id=" + postId + " не найден");
        }

        String sanitizedText = sanitizeText(text);
        long commentId = commentDao.save(postId, sanitizedText);
        return new CommentResponse(commentId, sanitizedText);
    }

    @Override
    @Transactional
    public boolean delete(long postId, long commentId) {
        return commentDao.deleteById(postId, commentId);
    }

    @Override
    @Transactional
    public CommentResponse update(long postId, long commentId, String text) {
        String sanitizedText = sanitizeText(text);
        boolean updated = commentDao.updateById(postId, commentId, sanitizedText);
        if (!updated) {
            return null;
        }
        return new CommentResponse(commentId, sanitizedText);
    }

    private String sanitizeText(String text) {
        String sanitizedText = text == null ? "" : text.trim();
        if (sanitizedText.isBlank()) {
            throw new IllegalArgumentException("текст комментария не должен быть пустым");
        }
        return sanitizedText;
    }
}
