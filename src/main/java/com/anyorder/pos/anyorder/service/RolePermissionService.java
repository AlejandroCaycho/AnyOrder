package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Permission;
import com.anyorder.pos.anyorder.model.Role;
import com.anyorder.pos.anyorder.model.RolePermission;
import com.anyorder.pos.anyorder.repository.PermissionRepository;
import com.anyorder.pos.anyorder.repository.RolePermissionRepository;
import com.anyorder.pos.anyorder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RolePermission> findAll() {
        return rolePermissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RolePermission> findPermissionsByRole(Integer roleId) {
        // Validar que el rol existe
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Rol no encontrado con ID: " + roleId);
        }
        return rolePermissionRepository.findPermissionsByRole(roleId);
    }

    @Transactional(readOnly = true)
    public List<RolePermission> findRolesByPermission(Integer permissionId) {
        // Validar que el permiso existe
        if (!permissionRepository.existsById(permissionId)) {
            throw new RuntimeException("Permiso no encontrado con ID: " + permissionId);
        }
        return rolePermissionRepository.findRolesByPermission(permissionId);
    }

    @Transactional
    public RolePermission assignPermissionToRole(Integer roleId, Integer permissionId) {
        // Validar que el rol existe
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));

        // Validar que el permiso existe
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));

        // Validar que no esté ya asignado
        if (rolePermissionRepository.existsByIdIdRoleAndIdIdPermission(roleId, permissionId)) {
            throw new RuntimeException("El permiso ya está asignado a este rol");
        }

        RolePermission rolePermission = new RolePermission(role, permission);
        return rolePermissionRepository.save(rolePermission);
    }

    @Transactional
    public void removePermissionFromRole(Integer roleId, Integer permissionId) {
        // Validar que existe la relación
        if (!rolePermissionRepository.existsByIdIdRoleAndIdIdPermission(roleId, permissionId)) {
            throw new RuntimeException("El permiso no está asignado a este rol");
        }

        rolePermissionRepository.deleteByIdIdRoleAndIdIdPermission(roleId, permissionId);
    }

    @Transactional
    public void removeAllPermissionsFromRole(Integer roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Rol no encontrado con ID: " + roleId);
        }
        rolePermissionRepository.deleteByIdIdRole(roleId);
    }

    @Transactional
    public void removePermissionFromAllRoles(Integer permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new RuntimeException("Permiso no encontrado con ID: " + permissionId);
        }
        rolePermissionRepository.deleteByIdIdPermission(permissionId);
    }

    @Transactional(readOnly = true)
    public long countPermissionsInRole(Integer roleId) {
        return rolePermissionRepository.countByIdIdRole(roleId);
    }

    @Transactional(readOnly = true)
    public long countRolesWithPermission(Integer permissionId) {
        return rolePermissionRepository.countByIdIdPermission(permissionId);
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(Integer roleId, Integer permissionId) {
        return rolePermissionRepository.existsByIdIdRoleAndIdIdPermission(roleId, permissionId);
    }
}