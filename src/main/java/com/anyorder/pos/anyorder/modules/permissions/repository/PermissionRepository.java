package com.anyorder.pos.anyorder.modules.permissions.repository;

import com.anyorder.pos.anyorder.modules.permissions.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByNamePermission(String namePermission);
    boolean existsByNamePermission(String namePermission);
    List<Permission> findByStateTrue();
}
