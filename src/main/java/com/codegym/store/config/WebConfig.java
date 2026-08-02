package com.codegym.store.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Trỏ tới thư mục "uploads" ở thư mục gốc của project
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Nói với Spring Boot: "Hễ ai truy cập vào /images/... thì hãy lấy file trong thư mục uploads ra cho họ xem!"
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}
