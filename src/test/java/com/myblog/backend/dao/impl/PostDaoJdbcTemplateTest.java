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
        postDao.save("Пост A", "Текст A");
        postDao.save("Пост B", "Текст B");
        postDao.save("Пост C", "Текст C");

        List<Post> posts = postDao.findAll("", 1, 2);

        assertEquals(2, posts.size());
    }

    @Test
    void findAll_shouldFilterBySearch() {
        String marker = "marker_find_all";
        postDao.save("Заголовок " + marker, "Обычный текст");
        postDao.save("Другой пост", "Текст без маркера");

        List<Post> posts = postDao.findAll(marker, 1, 10);

        assertFalse(posts.isEmpty());
        assertTrue(posts.stream().allMatch(p ->
                p.getTitle().toLowerCase().contains(marker)
                        || p.getText().toLowerCase().contains(marker)
        ));
    }

    @Test
    void count_shouldReturnTotalForEmptySearch() {
        int before = postDao.count("");
        postDao.save("Count title", "Count text");

        int total = postDao.count("");

        assertEquals(before + 1, total);
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
        long id = postDao.save("ORIGINAL_TITLE", "ORIGINAL_TEXT");
        UpdatePostRequest request = new UpdatePostRequest(
                "UPDATED_TITLE",
                "UPDATED_TEXT",
                777,
                55
        );

        boolean updated = postDao.updateById(id, request);

        assertTrue(updated);

        Post changed = postDao.findById(id)
                .orElseThrow(() -> new AssertionError("Пост не найден после update"));

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

    @Test
    void findById_shouldReturnPost_whenIdExists() {
        long id = postDao.save("findById title", "findById text");

        java.util.Optional<Post> result = postDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("findById title", result.get().getTitle());
        assertEquals("findById text", result.get().getText());
    }

    @Test
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        java.util.Optional<Post> result = postDao.findById(999_999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void likes_shouldAllowOnlyOneLikePerUser_andUpdateCount() {
        long postId = postDao.save("Likes post", "Likes text");

        assertTrue(postDao.addLike(postId, "user-1"));
        assertFalse(postDao.addLike(postId, "user-1"));
        assertTrue(postDao.addLike(postId, "user-2"));

        assertEquals(2, postDao.getLikesCount(postId));

        assertTrue(postDao.removeLike(postId, "user-1"));
        assertFalse(postDao.removeLike(postId, "user-1"));

        assertEquals(1, postDao.getLikesCount(postId));
    }
}
