package com.anyorder.pos.anyorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = System.getProperty("user.dir");
        File baseDir = new File(projectPath);
        if (new File(baseDir, "AnyOrder-be").exists()) {
            baseDir = new File(baseDir, "AnyOrder-be");
        }
        File staticDir = new File(baseDir, "src/main/resources/static/usuarios/");
        String path = "file:" + staticDir.getAbsolutePath() + "/";
        
        registry.addResourceHandler("/usuarios/**")
                .addResourceLocations(path, "classpath:/static/usuarios/");
    }
}
