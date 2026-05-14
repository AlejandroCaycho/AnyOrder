package com.anyorder.pos.anyorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.context.properties.EnableConfigurationProperties(com.anyorder.pos.anyorder.config.ExternalConfig.class)
public class AnyorderApplication {
	public static void main(String[] args) {
		SpringApplication.run(AnyorderApplication.class, args);
	}
}
