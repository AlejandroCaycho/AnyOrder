package com.anyorder.pos.anyorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para servicios externos como RENIEC, SUNAT y Cloudinary.
 * Mapea las propiedades definidas en application.yml bajo el prefijo 'external'.
 */
@Configuration
@ConfigurationProperties(prefix = "external")
@Data
public class ExternalConfig {

    private ApiConfig api = new ApiConfig();
    private CloudinaryConfig cloudinary = new CloudinaryConfig();

    @Data
    public static class ApiConfig {
        private String token;
        private ServiceUrl reniec = new ServiceUrl();
        private ServiceUrl sunat = new ServiceUrl();
    }

    @Data
    public static class ServiceUrl {
        private String url;
    }

    @Data
    public static class CloudinaryConfig {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
    }
}
