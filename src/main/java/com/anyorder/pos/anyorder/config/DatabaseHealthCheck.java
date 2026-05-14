package com.anyorder.pos.anyorder.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Verifica la conexión a la base de datos al iniciar la aplicación.
 * Detiene la ejecución si no se puede establecer una conexión válida.
 */
@Component
@Slf4j
public class DatabaseHealthCheck {

    private final DataSource dataSource;

    public DatabaseHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("Conexión a la base de datos establecida correctamente.");
        } catch (Exception e) {
            String msg = e.getMessage();

            if (msg.contains("Access denied")) {
                log.error("Error de acceso: Usuario o contraseña de base de datos incorrectos.");
            } else if (msg.contains("Unknown database")) {
                log.error("Error: La base de datos especificada no existe.");
            } else if (msg.contains("Communications link failure")) {
                log.error("Error de comunicación: MySQL no está en ejecución o el puerto es incorrecto.");
            } else {
                log.error("Error crítico al conectar a la base de datos: {}", msg);
            }

            System.exit(1);
        }
    }
}