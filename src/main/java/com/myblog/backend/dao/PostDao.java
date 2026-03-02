package com.myblog.backend.dao;

import com.myblog.backend.model.domain.Post;
import java.util.List;
import java.util.Optional;

public interface PostDao {
    List<Post> findAll(String search, int pageNumber, int pageSize);
    int count(String search);

    long save(String title, String text);
    Optional<Post> findById(long id);

    boolean deleteById(long id);
}
