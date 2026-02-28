package com.myblog.backend.dao;

import com.myblog.backend.model.domain.Post;
import java.util.List;

public interface PostDao {
    List<Post> findAll(String search, int pageNumber, int pageSize);
    int count(String search);
}