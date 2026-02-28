package com.myblog.backend.dao.impl;

import com.myblog.backend.model.domain.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.myblog.backend.dao.PostDao;

import java.util.List;

@Repository
public class PostDaoJdbcTemplate implements PostDao {

    private final JdbcTemplate jdbcTemplate;

    public PostDaoJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Post> findAll(String search, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        String like = "%" + search.toLowerCase() + "%";
        return jdbcTemplate.query(
                """
                select id, title, text, likes_count, comments_count
                from posts
                where lower(title) like ? or lower(text) like ?
                order by id desc
                limit ? offset ?
                """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ),
                like, like, pageSize, offset
        );
    }

    @Override
    public int count(String search) {
        String like = "%" + search.toLowerCase() + "%";
        Integer value = jdbcTemplate.queryForObject(
                "select count(*) from posts where lower(title) like ? or lower(text) like ?",
                Integer.class,
                like, like
        );
        return value == null ? 0 : value;
    }
}