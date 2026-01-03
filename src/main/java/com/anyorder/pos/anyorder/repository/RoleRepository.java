package com.anyorder.pos.anyorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anyorder.pos.anyorder.model.Role;
import com.anyorder.pos.anyorder.model.Role.RoleType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByNameRole(String nameRole);

    boolean existsByNameRole(String nameRole);

    Optional<Role> findByNameRoleIgnoreCase(String nameRole);

    List<Role> findByStateTrue();

    List<Role> findByState(Boolean state);

    List<Role> findByRoleType(RoleType roleType);

    List<Role> findByRoleTypeAndStateTrue(RoleType roleType);

    List<Role> findByRoleTypeAndState(RoleType roleType, Boolean state);

    List<Role> findByNameRoleContainingIgnoreCase(String nameRole);

    List<Role> findByCanPlaceOrdersTrue();

    List<Role> findByCanViewPricesTrue();

    long countByStateTrue();

    long countByRoleType(RoleType roleType);
}