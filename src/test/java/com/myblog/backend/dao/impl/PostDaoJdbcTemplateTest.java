package com.myblog.backend.dao.impl;

import com.myblog.backend.config.DaoTestConfig;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DaoTestConfig.class)
class PostDaoJdbcTemplateTest {

    @Autowired
    private PostDao postDao;

    @Test
    void findAll_shouldReturnFirstPage() {
        List<Post> posts = postDao.findAll("", 1, 2);

        assertEquals(2, posts.size());
    }

    @Test
    void findAll_shouldFilterBySearch() {
        List<Post> posts = postDao.findAll("первый", 1, 10);

        assertFalse(posts.isEmpty());
        assertTrue(posts.stream().allMatch(p ->
                p.getTitle().toLowerCase().contains("первый")
                        || p.getText().toLowerCase().contains("первый")
        ));
    }

    @Test
    void count_shouldReturnTotalForEmptySearch() {
        int total = postDao.count("");

        assertTrue(total >= 1);
    }

    @Test
    void count_shouldReturnZeroForUnknownSearch() {
        int total = postDao.count("zzz_not_found_zzz");

        assertEquals(0, total);
    }
}