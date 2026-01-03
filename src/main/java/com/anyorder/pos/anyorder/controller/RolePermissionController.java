package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.RolePermission;
import com.anyorder.pos.anyorder.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
@Tag(name = "Permisos de Roles", description = "Gestión de asignación de permisos a roles")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping
    @Operation(summary = "Listar todas las asignaciones de permisos a roles")
    public ResponseEntity<List<RolePermission>> getAll() {
        return ResponseEntity.ok(rolePermissionService.findAll());
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Obtener todos los permisos de un rol")
    public ResponseEntity<List<RolePermission>> getPermissionsByRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(rolePermissionService.findPermissionsByRole(roleId));
    }

    @GetMapping("/permission/{permissionId}")
    @Operation(summary = "Obtener todos los roles que tienen un permiso")
    public ResponseEntity<List<RolePermission>> getRolesByPermission(@PathVariable Integer permissionId) {
        return ResponseEntity.ok(rolePermissionService.findRolesByPermission(permissionId));
    }

    @PostMapping("/assign")
    @Operation(summary = "Asignar permiso a rol")
    public ResponseEntity<RolePermission> assignPermissionToRole(
            @RequestParam Integer roleId,
            @RequestParam Integer permissionId) {
        RolePermission rolePermission = rolePermissionService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(rolePermission);
    }

    @DeleteMapping("/revoke")
    @Operation(summary = "Revocar permiso de un rol")
    public ResponseEntity<Map<String, String>> removePermissionFromRole(
            @RequestParam Integer roleId,
            @RequestParam Integer permissionId) {
        rolePermissionService.removePermissionFromRole(roleId, permissionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso removido del rol correctamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/role/{roleId}")
    @Operation(summary = "Remover todos los permisos de un rol")
    public ResponseEntity<Map<String, String>> removeAllPermissionsFromRole(@PathVariable Integer roleId) {
        rolePermissionService.removeAllPermissionsFromRole(roleId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Todos los permisos fueron removidos del rol");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/permission/{permissionId}")
    @Operation(summary = "Remover permiso de todos los roles")
    public ResponseEntity<Map<String, String>> removePermissionFromAllRoles(@PathVariable Integer permissionId) {
        rolePermissionService.removePermissionFromAllRoles(permissionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso removido de todos los roles");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{roleId}/count")
    @Operation(summary = "Contar permisos en un rol")
    public ResponseEntity<Map<String, Long>> countPermissionsInRole(@PathVariable Integer roleId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", rolePermissionService.countPermissionsInRole(roleId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/permission/{permissionId}/count")
    @Operation(summary = "Contar roles con un permiso")
    public ResponseEntity<Map<String, Long>> countRolesWithPermission(@PathVariable Integer permissionId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", rolePermissionService.countRolesWithPermission(permissionId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/has-permission")
    @Operation(summary = "Verificar si un rol tiene un permiso específico")
    public ResponseEntity<Map<String, Object>> hasPermission(
            @RequestParam Integer roleId,
            @RequestParam Integer permissionId) {
        Map<String, Object> response = new HashMap<>();
        response.put("hasPermission", rolePermissionService.hasPermission(roleId, permissionId));
        response.put("roleId", roleId);
        response.put("permissionId", permissionId);
        return ResponseEntity.ok(response);
    }
}