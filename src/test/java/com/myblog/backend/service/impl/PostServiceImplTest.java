package com.myblog.backend.service.impl;

import com.myblog.backend.BackendApplication;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = BackendApplication.class)
@Import(ServiceTestConfig.class)
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
        when(postDao.findAll("", List.of(), 1, 2)).thenReturn(List.of(
                new Post(1L, "Первый пост", "Текст 1", List.of("java", "spring"), 12, 3),
                new Post(2L, "Второй пост", "Текст 2", List.of("backend"), 5, 1)
        ));
        when(postDao.count("", List.of())).thenReturn(4);

        PostsPageResponse response = postService.getPosts("", 1, 2);

        assertEquals(2, response.posts().size());
        assertFalse(response.hasPrev());
        assertTrue(response.hasNext());
        assertEquals(2, response.lastPage());

        verify(postDao, times(1)).findAll("", List.of(), 1, 2);
        verify(postDao, times(1)).count("", List.of());
    }

    @Test
    void getPosts_shouldTruncateLongTextForPreview() {
        String longText = "a".repeat(140);
        when(postDao.findAll("", List.of(), 1, 5)).thenReturn(List.of(
                new Post(1L, "Первый пост", longText, List.of("java"), 0, 0)
        ));
        when(postDao.count("", List.of())).thenReturn(1);

        PostsPageResponse response = postService.getPosts("", 1, 5);

        assertEquals(129, response.posts().get(0).text().length());
        assertTrue(response.posts().get(0).text().endsWith("…"));
    }

    @Test
    void getPosts_emptySearchResult() {
        when(postDao.findAll("zzz", List.of(), 1, 5)).thenReturn(List.of());
        when(postDao.count("zzz", List.of())).thenReturn(0);

        PostsPageResponse response = postService.getPosts("zzz", 1, 5);

        assertTrue(response.posts().isEmpty());
        assertFalse(response.hasPrev());
        assertFalse(response.hasNext());
        assertEquals(1, response.lastPage());

        verify(postDao, times(1)).findAll("zzz", List.of(), 1, 5);
        verify(postDao, times(1)).count("zzz", List.of());
    }

    @Test
    void getPosts_shouldSplitSearchIntoTitleAndTags() {
        when(postDao.findAll("backend guide", List.of("java", "spring"), 1, 5)).thenReturn(List.of());
        when(postDao.count("backend guide", List.of("java", "spring"))).thenReturn(0);

        postService.getPosts("  backend   guide   #java   #spring  ", 1, 5);

        verify(postDao, times(1)).findAll("backend guide", List.of("java", "spring"), 1, 5);
        verify(postDao, times(1)).count("backend guide", List.of("java", "spring"));
    }

    @Test
    void getPosts_shouldIgnoreEmptyWordsAndBlankTags() {
        when(postDao.findAll("", List.of("java"), 1, 5)).thenReturn(List.of());
        when(postDao.count("", List.of("java"))).thenReturn(0);

        postService.getPosts("   #java   #   ", 1, 5);

        verify(postDao, times(1)).findAll("", List.of("java"), 1, 5);
        verify(postDao, times(1)).count("", List.of("java"));
    }

    @Test
    void createPost_success() {
        CreatePostRequest request = new CreatePostRequest("Новый пост", "Текст поста", List.of("java", "spring", "java"));

        when(postDao.save("Новый пост", "Текст поста", List.of("java", "spring"))).thenReturn(10L);
        when(postDao.findById(10L)).thenReturn(
                Optional.of(new Post(10L, "Новый пост", "Текст поста", List.of("java", "spring"), 0, 0))
        );

        PostPreviewResponse response = postService.createPost(request);

        assertEquals(10L, response.id());
        assertEquals("Новый пост", response.title());
        assertEquals(List.of("java", "spring"), response.tags());
        verify(postDao, times(1)).save("Новый пост", "Текст поста", List.of("java", "spring"));
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
    void updatePost_shouldReturnUpdatedDto_whenDaoUpdatedRow() {
        UpdatePostRequest request = new UpdatePostRequest(
                1L,
                "Обновлённый заголовок",
                "Обновлённый текст",
                100,
                10,
                List.of("java", " spring ", "java")
        );
        Post existing = new Post(1L, "Старый заголовок", "Старый текст", List.of("old"), 7, 2);
        Post updatedPost = new Post(1L, "Обновлённый заголовок", "Обновлённый текст", List.of("java", "spring"), 7, 2);

        when(postDao.findById(1L)).thenReturn(Optional.of(existing), Optional.of(updatedPost));
        when(postDao.updateById(eq(1L), any(UpdatePostRequest.class))).thenReturn(true);

        Optional<PostPreviewResponse> result = postService.updatePost(1L, request);

        assertTrue(result.isPresent());
        assertEquals("Обновлённый заголовок", result.get().title());
        assertEquals("Обновлённый текст", result.get().text());
        assertEquals(List.of("java", "spring"), result.get().tags());
        assertEquals(7, result.get().likesCount());
        assertEquals(2, result.get().commentsCount());
        verify(postDao, times(2)).findById(1L);
        verify(postDao, times(1)).updateById(eq(1L), any(UpdatePostRequest.class));
    }

    @Test
    void updatePost_shouldReturnEmpty_whenDaoFoundNothingToUpdate() {
        UpdatePostRequest request = new UpdatePostRequest(
                999L,
                "Любой заголовок",
                "Любой текст",
                1,
                0,
                List.of("java")
        );

        when(postDao.findById(999L)).thenReturn(Optional.empty());

        Optional<PostPreviewResponse> result = postService.updatePost(999L, request);

        assertTrue(result.isEmpty());
        verify(postDao, times(1)).findById(999L);
    }

    @Test
    void getPostById_shouldReturnMappedDto_whenPostExists() {
        Post post = new Post(5L, "Пятый пост", "Текст 5", List.of("java", "markdown"), 10, 2);
        when(postDao.findById(5L)).thenReturn(java.util.Optional.of(post));

        java.util.Optional<PostPreviewResponse> result = postService.getPostById(5L);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().id());
        assertEquals("Пятый пост", result.get().title());
        assertEquals(List.of("java", "markdown"), result.get().tags());
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
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 0, 0)));

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
    void getImage_shouldReturnStoredImage_whenImageExists() {
        byte[] storedImage = new byte[]{9, 8, 7};
        when(postDao.findImageById(1L)).thenReturn(Optional.of(storedImage));

        byte[] result = postService.getImage(1L);

        assertEquals(3, result.length);
        assertEquals(9, result[0]);
        verify(postDao, times(1)).findImageById(1L);
    }

    @Test
    void addLike_shouldReturnActualCount_whenLikeCreated() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 0, 0)));
        when(postDao.addLike(1L, "user-1")).thenReturn(true);
        when(postDao.getLikesCount(1L)).thenReturn(1);

        int count = postService.addLike(1L, "user-1");

        assertEquals(1, count);
        verify(postDao, times(1)).addLike(1L, "user-1");
        verify(postDao, times(1)).getLikesCount(1L);
    }

    @Test
    void addLike_shouldReturnActualCount_whenDuplicateLike() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 1, 0)));
        when(postDao.addLike(1L, "user-1")).thenReturn(false);
        when(postDao.getLikesCount(1L)).thenReturn(1);

        int count = postService.addLike(1L, "user-1");

        assertEquals(1, count);
        verify(postDao, times(1)).addLike(1L, "user-1");
        verify(postDao, times(1)).getLikesCount(1L);
    }

    @Test
    void removeLike_shouldReturnActualCount_whenLikeRemoved() {
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 2, 0)));
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

    @Test
    void updateImage_shouldReturnTrue_whenPostExists() {
        MultipartFile image = new MockMultipartFile("image", "cover.png", "image/png", new byte[]{1, 2, 3});
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 0, 0)));
        when(postDao.updateImageById(eq(1L), argThat(bytes ->
                bytes != null && bytes.length == 3 && bytes[0] == 1 && bytes[1] == 2 && bytes[2] == 3
        ))).thenReturn(true);

        boolean result = postService.updateImage(1L, image);

        assertTrue(result);
        verify(postDao, times(1)).findById(1L);
        verify(postDao, times(1)).updateImageById(eq(1L), argThat(bytes ->
                bytes != null && bytes.length == 3 && bytes[0] == 1 && bytes[1] == 2 && bytes[2] == 3
        ));
    }

    @Test
    void updateImage_shouldReturnFalse_whenPostDoesNotExist() {
        MultipartFile image = new MockMultipartFile("image", "cover.png", "image/png", new byte[]{1, 2, 3});
        when(postDao.findById(999L)).thenReturn(Optional.empty());

        boolean result = postService.updateImage(999L, image);

        assertFalse(result);
        verify(postDao, times(1)).findById(999L);
    }

    @Test
    void updateImage_shouldThrow_whenFileEmpty() {
        MultipartFile image = new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);
        when(postDao.findById(1L)).thenReturn(Optional.of(new Post(1L, "t", "x", List.of(), 0, 0)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.updateImage(1L, image)
        );

        assertEquals("empty file", ex.getMessage());
        verify(postDao, times(1)).findById(1L);
    }
}

