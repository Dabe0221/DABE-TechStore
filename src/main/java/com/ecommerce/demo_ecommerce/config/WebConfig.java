package com.ecommerce.demo_ecommerce.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        Path uploadDirectory = Paths.get(
                System.getProperty("user.dir"),
                "uploads"
        );

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        uploadDirectory.toUri().toString(),
                        "classpath:/static/uploads/"
                );
    }
}