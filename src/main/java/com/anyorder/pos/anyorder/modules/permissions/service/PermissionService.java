package com.anyorder.pos.anyorder.modules.permissions.service;

import com.anyorder.pos.anyorder.modules.permissions.model.Permission;
import com.anyorder.pos.anyorder.modules.permissions.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public List<Permission> findAllActive() {
        return permissionRepository.findByStateTrue();
    }

    public Optional<Permission> findById(Integer id) {
        return permissionRepository.findById(id);
    }

    @Transactional
    public Permission create(Permission permission) {
        if (permissionRepository.existsByNamePermission(permission.getNamePermission())) {
            throw new IllegalArgumentException("Ya existe un permiso con ese nombre");
        }
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission update(Integer id, Permission permissionDetails) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));

        if (!permission.getNamePermission().equals(permissionDetails.getNamePermission()) &&
                permissionRepository.existsByNamePermission(permissionDetails.getNamePermission())) {
            throw new IllegalArgumentException("Ya existe un permiso con ese nombre");
        }

        permission.setNamePermission(permissionDetails.getNamePermission());
        permission.setDescription(permissionDetails.getDescription());
        permission.setState(permissionDetails.getState());

        return permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
        permission.setState(false);
        permissionRepository.save(permission);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permiso no encontrado con ID: " + id);
        }
        permissionRepository.deleteById(id);
    }

    @Transactional
    public Permission activate(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
        permission.setState(true);
        return permissionRepository.save(permission);
    }
}
