package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROLE_PERMISSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @EmbeddedId
    private RolePermissionId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idRole")
    @JoinColumn(name = "ID_ROLE", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idRole")
    @JsonIdentityReference(alwaysAsId = true)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idPermission")
    @JoinColumn(name = "ID_PERMISSION", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idPermission")
    @JsonIdentityReference(alwaysAsId = true)
    private Permission permission;

    // Constructor auxiliar
    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
        this.id = new RolePermissionId(role.getIdRole(), permission.getIdPermission());
    }
}