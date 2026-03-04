package com.myblog.backend.service.impl;

import com.myblog.backend.model.dto.response.CommentResponse;
import com.myblog.backend.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Override
    public List<CommentResponse> getByPostId(long postId) {
        return Collections.emptyList();
    }
}