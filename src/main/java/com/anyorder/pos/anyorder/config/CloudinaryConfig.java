package com.anyorder.pos.anyorder.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final ExternalConfig externalConfig;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", externalConfig.getCloudinary().getCloudName());
        config.put("api_key", externalConfig.getCloudinary().getApiKey());
        config.put("api_secret", externalConfig.getCloudinary().getApiSecret());
        return new Cloudinary(config);
    }
}
