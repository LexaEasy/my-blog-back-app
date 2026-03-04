package com.myblog.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.backend.service.CommentService;
import com.myblog.backend.service.PostService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ControllerTestConfig {

    @Bean
    @Primary
    public PostService postService() {
        return Mockito.mock(PostService.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public CommentService commentService() {
        return Mockito.mock(CommentService.class);
    }
}

