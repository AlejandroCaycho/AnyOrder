package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PERMISSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERMISSION")
    private Integer idPermission;

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    @Column(name = "NAME_PERMISSION", nullable = false, unique = true, length = 100)
    private String namePermission;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "STATE", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean state = true;

    @PrePersist
    protected void onCreate() {
        if (this.state == null) {
            this.state = true;
        }
    }
}
