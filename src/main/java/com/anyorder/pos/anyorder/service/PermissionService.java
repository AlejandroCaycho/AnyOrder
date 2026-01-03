package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Permission;
import com.anyorder.pos.anyorder.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Permission> findAllActive() {
        return permissionRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Permission findById(Integer id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Permission findByName(String name) {
        return permissionRepository.findByNamePermission(name)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con nombre: " + name));
    }

    @Transactional
    public Permission create(Permission permission) {
        // Validar nombre
        if (permission.getNamePermission() == null || permission.getNamePermission().trim().length() < 3) {
            throw new RuntimeException("El nombre del permiso debe tener al menos 3 caracteres");
        }

        // Validar unicidad
        if (permissionRepository.existsByNamePermission(permission.getNamePermission())) {
            throw new RuntimeException("Ya existe un permiso con el nombre: " + permission.getNamePermission());
        }

        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission update(Integer id, Permission permissionData) {
        Permission permission = findById(id);

        // Validar nombre único si cambió
        if (!permission.getNamePermission().equals(permissionData.getNamePermission()) &&
                permissionRepository.existsByNamePermission(permissionData.getNamePermission())) {
            throw new RuntimeException("Ya existe un permiso con el nombre: " + permissionData.getNamePermission());
        }

        if (permissionData.getNamePermission() != null &&
                permissionData.getNamePermission().trim().length() >= 3) {
            permission.setNamePermission(permissionData.getNamePermission());
        }

        if (permissionData.getDescription() != null) {
            permission.setDescription(permissionData.getDescription());
        }

        if (permissionData.getState() != null) {
            permission.setState(permissionData.getState());
        }

        return permissionRepository.save(permission);
    }

    @Transactional
    public void deactivate(Integer id) {
        Permission permission = findById(id);
        permission.setState(false);
        permissionRepository.save(permission);
    }

    @Transactional
    public void activate(Integer id) {
        Permission permission = findById(id);
        permission.setState(true);
        permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Integer id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permiso no encontrado con ID: " + id);
        }
        permissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Permission> searchByName(String name) {
        return permissionRepository.findByNamePermissionContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return permissionRepository.countByStateTrue();
    }
}
