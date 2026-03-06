package com.myblog.backend.dao.impl;

import com.myblog.backend.dao.CommentDao;
import com.myblog.backend.model.dto.response.CommentResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
public class CommentDaoJdbcTemplate implements CommentDao {

    private final JdbcTemplate jdbcTemplate;

    public CommentDaoJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CommentResponse> findByPostId(long postId) {
        return jdbcTemplate.query(
                """
                select id, text
                from comments
                where post_id = ?
                order by id asc
                """,
                (rs, rowNum) -> new CommentResponse(
                        rs.getLong("id"),
                        rs.getString("text")
                ),
                postId
        );
    }

    @Override
    public long save(long postId, String text) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    "insert into comments(post_id, text) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, postId);
            ps.setString(2, text);
            return ps;
        }, keyHolder);

        jdbcTemplate.update(
                "update posts set comments_count = comments_count + 1 where id = ?",
                postId
        );

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить id созданного комментария");
        }
        return key.longValue();
    }

    @Override
    public boolean deleteById(long postId, long commentId) {
        int deleted = jdbcTemplate.update(
                "delete from comments where id = ? and post_id = ?",
                commentId,
                postId
        );
        if (deleted == 0) {
            return false;
        }

        jdbcTemplate.update(
                """
                update posts
                set comments_count = case
                    when comments_count > 0 then comments_count - 1
                    else 0
                end
                where id = ?
                """,
                postId
        );

        return true;
    }

    @Override
    public boolean updateById(long postId, long commentId, String text) {
        int updated = jdbcTemplate.update(
                "update comments set text = ? where id = ? and post_id = ?",
                text,
                commentId,
                postId
        );
        return updated > 0;
    }
}
