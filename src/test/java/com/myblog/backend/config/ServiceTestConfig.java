package com.myblog.backend.config;

import com.myblog.backend.dao.CommentDao;
import com.myblog.backend.dao.PostDao;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class ServiceTestConfig {

    @Bean
    @Primary
    public PostDao mockPostDao() {
        return Mockito.mock(PostDao.class);
    }

    @Bean
    @Primary
    public CommentDao mockCommentDao() {
        return Mockito.mock(CommentDao.class);
    }
}
