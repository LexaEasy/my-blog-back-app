package com.myblog.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.backend.config.ControllerTestConfig;
import com.myblog.backend.config.WebConfig;
import com.myblog.backend.model.dto.request.UpdatePostRequest;
import com.myblog.backend.model.dto.response.CommentResponse;
import com.myblog.backend.model.dto.response.PostPreviewResponse;
import com.myblog.backend.model.dto.response.PostsPageResponse;
import com.myblog.backend.service.CommentService;
import com.myblog.backend.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebConfig.class, ControllerTestConfig.class})
@WebAppConfiguration
class PostControllerMvcTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(postService, commentService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getPosts_shouldReturn200AndJson() throws Exception {
        PostsPageResponse response = new PostsPageResponse(
                List.of(
                        new PostPreviewResponse(1L, "Первый пост", "Текст 1", List.of("java", "spring"), 12, 3),
                        new PostPreviewResponse(2L, "Второй пост", "Текст 2", List.of("backend"), 5, 1)
                ),
                true,
                false,
                2
        );

        when(postService.getPosts("", 1, 2)).thenReturn(response);

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.lastPage").value(2));
    }

    @Test
    void createPost_shouldReturn201AndJson() throws Exception {
        PostPreviewResponse response = new PostPreviewResponse(
                10L, "Новый пост", "Текст поста", java.util.List.of("java", "spring"), 0, 0
        );

        when(postService.createPost(any())).thenReturn(response);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                              {"title":"Новый пост","text":"Текст поста","tags":["java","spring"]}
                              """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Новый пост"))
                .andExpect(jsonPath("$.text").value("Текст поста"))
                .andExpect(jsonPath("$.tags[0]").value("java"))
                .andExpect(jsonPath("$.tags[1]").value("spring"));
    }

    @Test
    void deletePost_shouldReturn204() throws Exception {
        doNothing().when(postService).deletePost(10L);

        mockMvc.perform(delete("/api/posts/{id}", 10L))
                .andExpect(status().isOk());

        verify(postService, times(1)).deletePost(10L);
    }

    @Test
    void updatePost_shouldReturn200AndJson_whenUpdated() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                1L,
                "Новый title",
                "Новый text",
                10,
                2,
                List.of("java", "spring")
        );
        PostPreviewResponse response = new PostPreviewResponse(
                1L,
                "Новый title",
                "Новый text",
                List.of("java", "spring"),
                12,
                3
        );

        when(postService.updatePost(eq(1L), any(UpdatePostRequest.class)))
                .thenReturn(java.util.Optional.of(response));

        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Новый title"))
                .andExpect(jsonPath("$.text").value("Новый text"))
                .andExpect(jsonPath("$.tags[0]").value("java"))
                .andExpect(jsonPath("$.tags[1]").value("spring"));

        verify(postService, times(1)).updatePost(eq(1L), any(UpdatePostRequest.class));
    }

    @Test
    void updatePost_shouldReturn404_whenPostNotFound() throws Exception {
        UpdatePostRequest request = new UpdatePostRequest(
                999L,
                "Новый title",
                "Новый text",
                10,
                2,
                List.of("java")
        );

        when(postService.updatePost(eq(999L), any(UpdatePostRequest.class)))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).updatePost(eq(999L), any(UpdatePostRequest.class));
    }

    @Test
    void uploadImage_shouldReturn200AndOk_whenImageUpdated() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "cover.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );

        when(postService.updateImage(eq(1L), any())).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/posts/{id}/image", 1L)
                        .file(image))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(postService, times(1)).updateImage(eq(1L), any());
    }

    @Test
    void uploadImage_shouldReturn404_whenPostNotFound() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "cover.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3}
        );

        when(postService.updateImage(eq(999L), any())).thenReturn(false);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/posts/{id}/image", 999L)
                        .file(image))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).updateImage(eq(999L), any());
    }

    @Test
    void uploadImage_shouldReturn400AndText_whenFileEmpty() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "empty.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/posts/{id}/image", 1L)
                        .file(image))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty file"));

        verify(postService, never()).updateImage(eq(1L), any());
    }

    @Test
    void addLike_shouldReturnActualCount() throws Exception {
        when(postService.addLike(1L, "user-1")).thenReturn(7);

        mockMvc.perform(post("/api/posts/{id}/likes", 1L)
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));

        verify(postService, times(1)).addLike(1L, "user-1");
    }

    @Test
    void removeLike_shouldReturnActualCount() throws Exception {
        when(postService.removeLike(1L, "user-1")).thenReturn(6);

        mockMvc.perform(delete("/api/posts/{id}/likes", 1L)
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("6"));

        verify(postService, times(1)).removeLike(1L, "user-1");
    }

    @Test
    void addLike_shouldUseFallbackUserKey_whenHeaderMissing() throws Exception {
        when(postService.addLike(eq(1L), anyString())).thenReturn(1);

        mockMvc.perform(post("/api/posts/{id}/likes", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(postService, times(1)).addLike(eq(1L), anyString());
    }

    @Test
    void getLikesCount_shouldReturnActualCount() throws Exception {
        when(postService.getLikesCount(1L)).thenReturn(9);

        mockMvc.perform(get("/api/posts/{id}/likes", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("9"));

        verify(postService, times(1)).getLikesCount(1L);
    }

    @Test
    void getImage_shouldReturn200AndPng_whenPostExists() throws Exception {
        byte[] image = new byte[]{1, 2, 3, 4};
        when(postService.exists(1L)).thenReturn(true);
        when(postService.getImage(1L)).thenReturn(image);

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(image));

        verify(postService, times(1)).exists(1L);
        verify(postService, times(1)).getImage(1L);
    }

    @Test
    void getImage_shouldReturn404_whenPostNotFound() throws Exception {
        when(postService.exists(999L)).thenReturn(false);

        mockMvc.perform(get("/api/posts/{id}/image", 999L))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).exists(999L);
        verify(postService, never()).getImage(999L);
    }

    @Test
    void getPostById_shouldReturn200AndJson_whenExists() throws Exception {
        PostPreviewResponse response = new PostPreviewResponse(
                5L, "Пятый пост", "Текст 5", List.of("java", "markdown"), 0, 0
        );
        when(postService.getPostById(5L)).thenReturn(java.util.Optional.of(response));

        mockMvc.perform(post("/api/posts/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Пятый пост"))
                .andExpect(jsonPath("$.tags[0]").value("java"));
    }

    @Test
    void getPostById_shouldReturn404_whenNotExists() throws Exception {
        when(postService.getPostById(999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/api/posts/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_shouldReturnEmptyArray() throws Exception {
        when(commentService.getByPostId(5L)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/{id}/comments", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(commentService, times(1)).getByPostId(5L);
    }

    @Test
    void getComments_shouldReturnArrayWithItems() throws Exception {
        List<CommentResponse> comments = List.of(
                new CommentResponse(1L, "Первый комментарий", 5L),
                new CommentResponse(2L, "Второй комментарий", 5L)
        );
        when(commentService.getByPostId(5L)).thenReturn(comments);

        mockMvc.perform(get("/api/posts/{id}/comments", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Первый комментарий"))
                .andExpect(jsonPath("$[0].postId").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Второй комментарий"))
                .andExpect(jsonPath("$[1].postId").value(5));

        verify(commentService, times(1)).getByPostId(5L);
    }

    @Test
    void addComment_shouldReturn201AndJson() throws Exception {
        when(commentService.add(5L, "Новый комментарий"))
                .thenReturn(new CommentResponse(11L, "Новый комментарий", 5L));

        mockMvc.perform(post("/api/posts/{id}/comments", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Новый комментарий","postId":5}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.text").value("Новый комментарий"))
                .andExpect(jsonPath("$.postId").value(5));

        verify(commentService, times(1)).add(5L, "Новый комментарий");
    }

    @Test
    void addComment_shouldReturn400AndMessage_whenTextIsInvalid() throws Exception {
        when(commentService.add(5L, "   "))
                .thenThrow(new IllegalArgumentException("text комментария не должен быть пустым"));

        mockMvc.perform(post("/api/posts/{id}/comments", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("text комментария не должен быть пустым"));

        verify(commentService, times(1)).add(5L, "   ");
    }

    @Test
    void getComment_shouldReturn200AndJson_whenExists() throws Exception {
        when(commentService.getById(5L, 9L))
                .thenReturn(java.util.Optional.of(new CommentResponse(9L, "Комментарий", 5L)));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 5L, 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.text").value("Комментарий"))
                .andExpect(jsonPath("$.postId").value(5));

        verify(commentService, times(1)).getById(5L, 9L);
    }

    @Test
    void getComment_shouldReturn404_whenNotExists() throws Exception {
        when(commentService.getById(5L, 999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 5L, 999L))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).getById(5L, 999L);
    }

    @Test
    void updateComment_shouldReturn200AndJson_whenUpdated() throws Exception {
        when(commentService.update(5L, 9L, "Обновлённый комментарий"))
                .thenReturn(new CommentResponse(9L, "Обновлённый комментарий", 5L));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 5L, 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":9,"text":"Обновлённый комментарий","postId":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.text").value("Обновлённый комментарий"))
                .andExpect(jsonPath("$.postId").value(5));

        verify(commentService, times(1)).update(5L, 9L, "Обновлённый комментарий");
    }

    @Test
    void updateComment_shouldReturn404_whenCommentNotFound() throws Exception {
        when(commentService.update(5L, 999L, "Обновлённый комментарий"))
                .thenReturn(null);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 5L, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":999,"text":"Обновлённый комментарий","postId":5}
                                """))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).update(5L, 999L, "Обновлённый комментарий");
    }

    @Test
    void updateComment_shouldReturn400AndMessage_whenTextIsInvalid() throws Exception {
        when(commentService.update(5L, 9L, "   "))
                .thenThrow(new IllegalArgumentException("text комментария не должен быть пустым"));

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 5L, 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":9,"text":"   ","postId":5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("text комментария не должен быть пустым"));

        verify(commentService, times(1)).update(5L, 9L, "   ");
    }

    @Test
    void deleteComment_shouldReturn200_whenDeleted() throws Exception {
        when(commentService.delete(5L, 9L)).thenReturn(true);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 5L, 9L))
                .andExpect(status().isOk());

        verify(commentService, times(1)).delete(5L, 9L);
    }

    @Test
    void deleteComment_shouldReturn404_whenCommentNotFound() throws Exception {
        when(commentService.delete(5L, 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 5L, 999L))
                .andExpect(status().isNotFound());

        verify(commentService, times(1)).delete(5L, 999L);
    }
}
