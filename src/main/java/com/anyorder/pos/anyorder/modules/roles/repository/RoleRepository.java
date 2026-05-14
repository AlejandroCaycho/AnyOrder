package com.anyorder.pos.anyorder.modules.roles.repository;

import com.anyorder.pos.anyorder.modules.roles.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findAll();

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findById(Integer id);

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByNameRole(String nameRole);

    boolean existsByNameRole(String nameRole);

    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findByStateTrue();

    @EntityGraph(attributePaths = {"permissions"})
    List<Role> findByRoleType(Role.RoleType roleType);
}
