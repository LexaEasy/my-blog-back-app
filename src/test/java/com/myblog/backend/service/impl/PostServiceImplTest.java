package com.myblog.backend.service.impl;

import com.myblog.backend.config.ServiceTestConfig;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
class PostServiceImplTest {

    @Autowired
    private PostDao postDao;

    @Autowired
    private PostService postService;

    @BeforeEach
    void resetMocks() {
        reset(postDao);
    }

    @Test
    void getPosts_success() {
        when(postDao.findAll("", 1, 2)).thenReturn(List.of(
                new Post(1L, "Первый пост", "Текст 1", 12, 3),
                new Post(2L, "Второй пост", "Текст 2", 5, 1)
        ));
        when(postDao.count("")).thenReturn(4);

        PostsPageResponse response = postService.getPosts("", 1, 2);

        assertEquals(2, response.posts().size());
        assertFalse(response.hasPrev());
        assertTrue(response.hasNext());
        assertEquals(2, response.lastPage());

        verify(postDao, times(1)).findAll("", 1, 2);
        verify(postDao, times(1)).count("");
    }

    @Test
    void getPosts_emptySearchResult() {
        when(postDao.findAll("zzz", 1, 5)).thenReturn(List.of());
        when(postDao.count("zzz")).thenReturn(0);

        PostsPageResponse response = postService.getPosts("zzz", 1, 5);

        assertTrue(response.posts().isEmpty());
        assertFalse(response.hasPrev());
        assertFalse(response.hasNext());
        assertEquals(1, response.lastPage());

        verify(postDao, times(1)).findAll("zzz", 1, 5);
        verify(postDao, times(1)).count("zzz");
    }

    @Test
    void createPost_success() {
        CreatePostRequest request = new CreatePostRequest("Новый пост", "Текст поста");

        when(postDao.save("Новый пост", "Текст поста")).thenReturn(10L);
        when(postDao.findById(10L)).thenReturn(
                Optional.of(new Post(10L, "Новый пост", "Текст поста", 0, 0))
        );

        PostPreviewResponse response = postService.createPost(request);

        assertEquals(10L, response.id());
        assertEquals("Новый пост", response.title());
        verify(postDao, times(1)).save("Новый пост", "Текст поста");
        verify(postDao, times(1)).findById(10L);
    }

    @Test
    void deletePost_success() {
        when(postDao.deleteById(10L)).thenReturn(true);

        postService.deletePost(10L);

        verify(postDao, times(1)).deleteById(10L);
    }

    @Test
    void deletePost_notFound_shouldThrow() {
        when(postDao.deleteById(999L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.deletePost(999L)
        );

        assertTrue(ex.getMessage().contains("не найден"));
        verify(postDao, times(1)).deleteById(999L);
    }
}
