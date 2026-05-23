package com.anyorder.pos.anyorder.modules.roles.service;

import com.anyorder.pos.anyorder.modules.roles.model.Role;
import com.anyorder.pos.anyorder.modules.roles.repository.RoleRepository;
import com.anyorder.pos.anyorder.modules.permissions.model.Permission;
import com.anyorder.pos.anyorder.modules.permissions.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public List<Role> findAllActive() {
        return roleRepository.findByStateTrue();
    }

    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role create(Role role) {
        if (roleRepository.existsByNameRole(role.getNameRole())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Integer id, Role roleDetails) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        if (!role.getNameRole().equals(roleDetails.getNameRole()) &&
                roleRepository.existsByNameRole(roleDetails.getNameRole())) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre");
        }

        role.setNameRole(roleDetails.getNameRole());
        role.setDescription(roleDetails.getDescription());
        role.setRoleType(roleDetails.getRoleType());
        role.setCanViewMenu(roleDetails.getCanViewMenu());
        role.setCanPlaceOrders(roleDetails.getCanPlaceOrders());
        role.setCanViewPrices(roleDetails.getCanViewPrices());
        role.setState(roleDetails.getState());
        
        if (roleDetails.getPermissions() != null) {
            role.setPermissions(roleDetails.getPermissions());
        }

        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        role.setState(false);
        roleRepository.save(role);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Rol no encontrado con ID: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Transactional
    public Role activate(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        role.setState(true);
        return roleRepository.save(role);
    }

    @Transactional
    public Role assignPermission(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));

        role.getPermissions().add(permission);
        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermission(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado con ID: " + permissionId));

        role.getPermissions().remove(permission);
        return roleRepository.save(role);
    }
}
