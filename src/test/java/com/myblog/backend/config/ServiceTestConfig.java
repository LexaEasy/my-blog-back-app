package com.myblog.backend.config;

import com.myblog.backend.dao.PostDao;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan("com.myblog.backend.service")
public class ServiceTestConfig {

    @Bean
    @Primary
    public PostDao mockPostDao() {
        return Mockito.mock(PostDao.class);
    }
}