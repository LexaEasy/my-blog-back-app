package com.myblog.backend.config;

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
}