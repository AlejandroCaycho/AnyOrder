package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Role;
import com.anyorder.pos.anyorder.model.Role.RoleType;
import com.anyorder.pos.anyorder.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> findAllActive() {
        return roleRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Role findById(Integer id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
    }

    @Transactional
    public Role create(Role role) {

        if (roleRepository.existsByNameRole(role.getNameRole())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + role.getNameRole());
        }

        if (role.getNameRole() == null || role.getNameRole().trim().length() < 2) {
            throw new RuntimeException("El nombre del rol debe tener al menos 2 caracteres");
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Integer id, Role roleData) {
        Role role = findById(id);

        if (!role.getNameRole().equals(roleData.getNameRole()) &&
                roleRepository.existsByNameRole(roleData.getNameRole())) {
            throw new RuntimeException("Ya existe un rol con el nombre: " + roleData.getNameRole());
        }

        role.setNameRole(roleData.getNameRole());
        role.setDescription(roleData.getDescription());

        if (roleData.getRoleType() != null) {
            role.setRoleType(roleData.getRoleType());
        }

        if (roleData.getCanViewMenu() != null) {
            role.setCanViewMenu(roleData.getCanViewMenu());
        }

        if (roleData.getCanPlaceOrders() != null) {
            role.setCanPlaceOrders(roleData.getCanPlaceOrders());
        }

        if (roleData.getCanViewPrices() != null) {
            role.setCanViewPrices(roleData.getCanViewPrices());
        }

        if (roleData.getState() != null) {
            role.setState(roleData.getState());
        }

        return roleRepository.save(role);
    }

    @Transactional
    public void deactivate(Integer id) {
        Role role = findById(id);
        role.setState(false);
        roleRepository.save(role);
    }

    @Transactional
    public void activate(Integer id) {
        Role role = findById(id);
        role.setState(true);
        roleRepository.save(role);
    }

    @Transactional
    public void delete(Integer id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Rol no encontrado con ID: " + id);
        }
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Role findByName(String name) {
        return roleRepository.findByNameRole(name)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con nombre: " + name));
    }

    @Transactional(readOnly = true)
    public List<Role> findByType(RoleType roleType) {
        return roleRepository.findByRoleType(roleType);
    }

    @Transactional(readOnly = true)
    public List<Role> findEmployeeRolesActive() {
        return roleRepository.findByRoleTypeAndStateTrue(RoleType.EMPLOYEE);
    }

    @Transactional(readOnly = true)
    public List<Role> findCustomerRolesActive() {
        return roleRepository.findByRoleTypeAndStateTrue(RoleType.CUSTOMER);
    }

    @Transactional(readOnly = true)
    public List<Role> searchByName(String name) {
        return roleRepository.findByNameRoleContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Role> findRolesCanPlaceOrders() {
        return roleRepository.findByCanPlaceOrdersTrue();
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return roleRepository.countByStateTrue();
    }

    @Transactional(readOnly = true)
    public long countByType(RoleType roleType) {
        return roleRepository.countByRoleType(roleType);
    }
}