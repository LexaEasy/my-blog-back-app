package com.myblog.backend.dao.impl;

import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;
import java.util.Optional;

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

    @Override
    public long save(String title, String text) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    "insert into posts(title, text, likes_count, comments_count) values (?, ?, 0, 0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, title);
            ps.setString(2, text);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить id созданного поста");
        }
        return key.longValue();
    }

    @Override
    public Optional<Post> findById(long id) {
        List<Post> list = jdbcTemplate.query(
                """
                select id, title, text, likes_count, comments_count
                from posts
                where id = ?
                """,
                (rs, rowNum) -> new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("likes_count"),
                        rs.getInt("comments_count")
                ),
                id
        );
        return list.stream().findFirst();
    }

    @Override
    public boolean deleteById(long id) {
        int updated = jdbcTemplate.update(
                "delete from posts where id = ?",
                id
        );
        return updated > 0;
    }

    @Override
    public boolean updateById(long id, UpdatePostRequest request) {
        String sql = """
          update posts
             set title = ?, text = ?, likes_count = ?, comments_count = ?
           where id = ?
          """;
        int updated = jdbcTemplate.update(
                sql,
                request.getTitle(),
                request.getText(),
                request.getLikes(),
                request.getComments(),
                id
        );
        return updated > 0;
    }
}
