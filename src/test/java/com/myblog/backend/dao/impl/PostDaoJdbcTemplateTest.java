package com.myblog.backend.dao.impl;

import com.myblog.backend.config.DaoTestConfig;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void save_shouldInsertPost() {
        int before = postDao.count("");
        long id = postDao.save("DAO пост", "DAO текст");

        assertTrue(id > 0);
        int after = postDao.count("");
        assertEquals(before + 1, after);

        List<Post> list = postDao.findAll("dao", 1, 10);
        assertTrue(list.stream().anyMatch(p -> p.getId() == id));
    }

    @Test
    void deleteById_shouldRemovePost() {
        long id = postDao.save("to delete", "text");
        assertTrue(postDao.findById(id).isPresent());

        boolean deleted = postDao.deleteById(id);

        assertTrue(deleted);
        assertTrue(postDao.findById(id).isEmpty());
    }

    @Test
    void updateById_shouldReturnTrue_andChangeData_whenIdExists() {
        // Берём существующий пост из schema.sql (обычно id=1 есть)
        long id = 1L;
        UpdatePostRequest request = new UpdatePostRequest(
                "UPDATED_TITLE",
                "UPDATED_TEXT",
                777,
                55
        );

        boolean updated = postDao.updateById(id, request);

        assertTrue(updated);

        List<Post> page = postDao.findAll("", 1, 50);
        Post changed = page.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Пост с id=1 не найден после update"));

        assertEquals("UPDATED_TITLE", changed.getTitle());
        assertEquals("UPDATED_TEXT", changed.getText());
        assertEquals(777, changed.getLikesCount());
        assertEquals(55, changed.getCommentsCount());
    }

    @Test
    void updateById_shouldReturnFalse_whenIdDoesNotExist() {
        UpdatePostRequest request = new UpdatePostRequest(
                "NOPE",
                "NOPE",
                1,
                1
        );

        boolean updated = postDao.updateById(999_999L, request);

        assertFalse(updated);
    }
}
