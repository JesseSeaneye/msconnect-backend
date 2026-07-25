package com.msconnect.maintenancebackend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get("uploads").toAbsolutePath().toUri().toString();
        
        // Serves all uploaded images and videos dynamically over http://172.20.10.13:8080/uploads/...
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir);
    }
}
