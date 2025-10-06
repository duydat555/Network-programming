package com.example.hls_server.config;

import jakarta.annotation.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/hls/**")
//                .addResourceLocations("file:///D:/hls/")
//                .setCacheControl(CacheControl.maxAge(Duration.ofHours(12)).cachePublic());
    }
}
