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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        reset(postService, commentService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void getPosts_shouldReturn200AndJson() throws Exception {
        PostsPageResponse response = new PostsPageResponse(
                List.of(
                        new PostPreviewResponse(1L, "Первый пост", "Текст 1", List.of(), 12, 3),
                        new PostPreviewResponse(2L, "Второй пост", "Текст 2", List.of(), 5, 1)
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
                10L, "Новый пост", "Текст поста", java.util.List.of(), 0, 0
        );

        when(postService.createPost(any())).thenReturn(response);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                              {"title":"Новый пост","text":"Текст поста"}
                              """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Новый пост"))
                .andExpect(jsonPath("$.text").value("Текст поста"));
    }

    @Test
    void deletePost_shouldReturn204() throws Exception {
        doNothing().when(postService).deletePost(10L);

        mockMvc.perform(delete("/api/posts/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).deletePost(10L);
    }

    @Test
    void updatePost_shouldReturn204_whenUpdated() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        UpdatePostRequest request = new UpdatePostRequest(
                "Новый title",
                "Новый text",
                10,
                2
        );

        when(postService.updatePost(eq(1L), any(UpdatePostRequest.class))).thenReturn(true);

        mockMvc.perform(put("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(postService, times(1)).updatePost(eq(1L), any(UpdatePostRequest.class));
    }

    @Test
    void updatePost_shouldReturn404_whenPostNotFound() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        UpdatePostRequest request = new UpdatePostRequest(
                "Новый title",
                "Новый text",
                10,
                2
        );

        when(postService.updatePost(eq(999L), any(UpdatePostRequest.class))).thenReturn(false);

        mockMvc.perform(put("/api/posts/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(postService, times(1)).updatePost(eq(999L), any(UpdatePostRequest.class));
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
                5L, "Пятый пост", "Текст 5", List.of(), 0, 0
        );
        when(postService.getPostById(5L)).thenReturn(java.util.Optional.of(response));

        mockMvc.perform(get("/api/posts/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Пятый пост"));
    }

    @Test
    void getPostById_shouldReturn404_whenNotExists() throws Exception {
        when(postService.getPostById(999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/posts/{id}", 999L))
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
                new CommentResponse(1L, "Первый комментарий"),
                new CommentResponse(2L, "Второй комментарий")
        );
        when(commentService.getByPostId(5L)).thenReturn(comments);

        mockMvc.perform(get("/api/posts/{id}/comments", 5L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Первый комментарий"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Второй комментарий"));

        verify(commentService, times(1)).getByPostId(5L);
    }
}
