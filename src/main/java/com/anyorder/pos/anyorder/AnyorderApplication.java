package com.anyorder.pos.anyorder;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.boot.context.properties.EnableConfigurationProperties(com.anyorder.pos.anyorder.config.ExternalConfig.class)
public class AnyorderApplication {
	public static void main(String[] args) {
		// Carga manual de .env para asegurar disponibilidad de variables
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
		
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});

		SpringApplication.run(AnyorderApplication.class, args);
	}
}
