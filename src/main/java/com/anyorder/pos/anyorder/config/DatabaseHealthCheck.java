package com.anyorder.pos.anyorder.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthCheck {

    private final DataSource dataSource;

    public DatabaseHealthCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("Conectado a MySQL");
        } catch (Exception e) {
            String msg = e.getMessage();

            if (msg.contains("Access denied")) {
                System.err.println("\nERROR: Usuario o contraseña incorrectos");
            } else if (msg.contains("Unknown database")) {
                System.err.println("\nERROR: Base de datos 'AnyOrder' no existe");
            } else if (msg.contains("Communications link failure")) {
                System.err.println("\nERROR: No se puede conectar - MySQL no está corriendo o puerto incorrecto");
            } else if (msg.contains("No suitable driver")) {
                System.err.println("\nERROR: Driver MySQL no encontrado");
            } else {
                System.err.println("\nERROR: " + msg);
            }

            System.exit(1);
        }
    }
}