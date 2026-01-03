package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ROLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ROLE")
    private Integer idRole;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    @Column(name = "NAME_ROLE", nullable = false, unique = true, length = 255)
    private String nameRole;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_TYPE", nullable = false, length = 20)
    private RoleType roleType = RoleType.EMPLOYEE;

    @Column(name = "CAN_VIEW_MENU", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean canViewMenu = true;

    @Column(name = "CAN_PLACE_ORDERS", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean canPlaceOrders = false;

    @Column(name = "CAN_VIEW_PRICES", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean canViewPrices = true;

    @Column(name = "STATE", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean state = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum RoleType {
        EMPLOYEE,
        CUSTOMER
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = true;
        }
        if (this.roleType == null) {
            this.roleType = RoleType.EMPLOYEE;
        }
        if (this.canViewMenu == null) {
            this.canViewMenu = true;
        }
        if (this.canPlaceOrders == null) {
            this.canPlaceOrders = false;
        }
        if (this.canViewPrices == null) {
            this.canViewPrices = true;
        }
    }
}