package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Permission;
import com.anyorder.pos.anyorder.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Permisos", description = "Gestión de permisos del sistema")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Listar todos los permisos")
    public ResponseEntity<List<Permission>> getAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Listar permisos activos")
    public ResponseEntity<List<Permission>> getAllActive() {
        return ResponseEntity.ok(permissionService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener permiso por ID")
    public ResponseEntity<Permission> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Obtener permiso por nombre")
    public ResponseEntity<Permission> getByName(@PathVariable String name) {
        return ResponseEntity.ok(permissionService.findByName(name));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo permiso")
    public ResponseEntity<Permission> create(@Valid @RequestBody Permission permission) {
        Permission created = permissionService.create(permission);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar permiso")
    public ResponseEntity<Permission> update(@PathVariable Integer id, @Valid @RequestBody Permission permission) {
        Permission updated = permissionService.update(id, permission);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar permiso")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        permissionService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso desactivado correctamente");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar permiso")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        permissionService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso activado correctamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar permiso")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        permissionService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Permiso eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar permisos por nombre")
    public ResponseEntity<List<Permission>> search(@RequestParam String name) {
        return ResponseEntity.ok(permissionService.searchByName(name));
    }

    @GetMapping("/count/active")
    @Operation(summary = "Contar permisos activos")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", permissionService.countActive());
        return ResponseEntity.ok(response);
    }
}