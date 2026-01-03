package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    Optional<Permission> findByNamePermission(String namePermission);

    boolean existsByNamePermission(String namePermission);

    Optional<Permission> findByNamePermissionIgnoreCase(String namePermission);

    List<Permission> findByStateTrue();

    List<Permission> findByStateFalse();

    List<Permission> findByState(Boolean state);

    List<Permission> findByNamePermissionContainingIgnoreCase(String namePermission);

    long countByStateTrue();
}