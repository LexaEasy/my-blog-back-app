package com.myblog.backend.service.impl;

import com.myblog.backend.config.ServiceTestConfig;
import com.myblog.backend.dao.PostDao;
import com.myblog.backend.model.domain.Post;
import com.myblog.backend.model.dto.request.CreatePostRequest;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
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

    @Test
    void updatePost_shouldReturnTrue_whenDaoUpdatedRow() {
        UpdatePostRequest request = new UpdatePostRequest(
                "Обновлённый заголовок",
                "Обновлённый текст",
                100,
                10
        );

        when(postDao.updateById(1L, request)).thenReturn(true);

        boolean result = postService.updatePost(1L, request);

        assertTrue(result);
        verify(postDao, times(1)).updateById(1L, request);
    }

    @Test
    void updatePost_shouldReturnFalse_whenDaoFoundNothingToUpdate() {
        UpdatePostRequest request = new UpdatePostRequest(
                "Любой заголовок",
                "Любой текст",
                1,
                0
        );

        when(postDao.updateById(999L, request)).thenReturn(false);

        boolean result = postService.updatePost(999L, request);

        assertFalse(result);
        verify(postDao, times(1)).updateById(999L, request);
    }

    @Test
    void getPostById_shouldReturnMappedDto_whenPostExists() {
        Post post = new Post(5L, "Пятый пост", "Текст 5", 10, 2);
        when(postDao.findById(5L)).thenReturn(java.util.Optional.of(post));

        java.util.Optional<PostPreviewResponse> result = postService.getPostById(5L);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().id());
        assertEquals("Пятый пост", result.get().title());
        verify(postDao, times(1)).findById(5L);
    }

    @Test
    void getPostById_shouldReturnEmpty_whenPostNotExists() {
        when(postDao.findById(999L)).thenReturn(Optional.empty());

        Optional<PostPreviewResponse> result = postService.getPostById(999L);

        assertTrue(result.isEmpty());
        verify(postDao, times(1)).findById(999L);
    }

    @Test
    void exists_shouldReturnTrue_whenPostExists() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", 0, 0)));

        boolean result = postService.exists(1L);

        assertTrue(result);
        verify(postDao, times(1)).findById(1L);
    }

    @Test
    void exists_shouldReturnFalse_whenPostNotExists() {
        when(postDao.findById(1L)).thenReturn(Optional.empty());

        boolean result = postService.exists(1L);

        assertFalse(result);
        verify(postDao, times(1)).findById(1L);
    }

    @Test
    void getImage_shouldReturnNonEmptyDefaultImage() {
        byte[] bytes = postService.getImage(1L);

        assertTrue(bytes != null && bytes.length > 0);
    }

    @Test
    void addLike_shouldReturnActualCount_whenLikeCreated() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", 0, 0)));
        when(postDao.addLike(1L, "user-1")).thenReturn(true);
        when(postDao.getLikesCount(1L)).thenReturn(1);

        int count = postService.addLike(1L, "user-1");

        assertEquals(1, count);
        verify(postDao, times(1)).addLike(1L, "user-1");
        verify(postDao, times(1)).getLikesCount(1L);
    }

    @Test
    void addLike_shouldReturnActualCount_whenDuplicateLike() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", 1, 0)));
        when(postDao.addLike(1L, "user-1")).thenReturn(false);
        when(postDao.getLikesCount(1L)).thenReturn(1);

        int count = postService.addLike(1L, "user-1");

        assertEquals(1, count);
        verify(postDao, times(1)).addLike(1L, "user-1");
        verify(postDao, times(1)).getLikesCount(1L);
    }

    @Test
    void removeLike_shouldReturnActualCount_whenLikeRemoved() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", 2, 0)));
        when(postDao.removeLike(1L, "user-1")).thenReturn(true);
        when(postDao.getLikesCount(1L)).thenReturn(1);

        int count = postService.removeLike(1L, "user-1");

        assertEquals(1, count);
        verify(postDao, times(1)).removeLike(1L, "user-1");
        verify(postDao, times(1)).getLikesCount(1L);
    }

    @Test
    void getLikesCount_shouldThrow_whenPostNotFound() {
        when(postDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> postService.getLikesCount(999L));
    }
}

