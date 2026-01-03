package com.anyorder.pos.anyorder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionId implements Serializable {

    @Column(name = "ID_ROLE")
    private Integer idRole;

    @Column(name = "ID_PERMISSION")
    private Integer idPermission;
}