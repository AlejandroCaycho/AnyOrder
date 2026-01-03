package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.RolePermission;
import com.anyorder.pos.anyorder.model.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    List<RolePermission> findByIdIdRole(Integer idRole);

    List<RolePermission> findByIdIdPermission(Integer idPermission);

    boolean existsByIdIdRoleAndIdIdPermission(Integer idRole, Integer idPermission);

    void deleteByIdIdRoleAndIdIdPermission(Integer idRole, Integer idPermission);

    void deleteByIdIdRole(Integer idRole);

    void deleteByIdIdPermission(Integer idPermission);

    long countByIdIdRole(Integer idRole);

    long countByIdIdPermission(Integer idPermission);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.idRole = :roleId ORDER BY rp.permission.namePermission ASC")
    List<RolePermission> findPermissionsByRole(@Param("roleId") Integer roleId);

    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.idPermission = :permissionId ORDER BY rp.role.nameRole ASC")
    List<RolePermission> findRolesByPermission(@Param("permissionId") Integer permissionId);
}
