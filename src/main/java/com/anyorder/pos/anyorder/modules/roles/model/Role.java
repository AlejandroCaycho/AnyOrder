package com.anyorder.pos.anyorder.modules.roles.model;

import com.anyorder.pos.anyorder.modules.permissions.model.Permission;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

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

    @NotNull(message = "El tipo de rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE_TYPE", nullable = false)
    private RoleType roleType = RoleType.EMPLOYEE;

    @Column(name = "CAN_VIEW_MENU", nullable = false)
    private Boolean canViewMenu = true;

    @Column(name = "CAN_PLACE_ORDERS", nullable = false)
    private Boolean canPlaceOrders = false;

    @Column(name = "CAN_VIEW_PRICES", nullable = false)
    private Boolean canViewPrices = true;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "ROLE_PERMISSIONS",
        joinColumns = @JoinColumn(name = "ID_ROLE"),
        inverseJoinColumns = @JoinColumn(name = "ID_PERMISSION")
    )
    private Set<Permission> permissions;

    public enum RoleType {
        EMPLOYEE, CUSTOMER
    }
}
