package com.myblog.backend.service.impl;

import com.myblog.backend.config.ServiceTestConfig;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
}
