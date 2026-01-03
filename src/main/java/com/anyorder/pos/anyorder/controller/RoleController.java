package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Role;
import com.anyorder.pos.anyorder.model.Role.RoleType;
import com.anyorder.pos.anyorder.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles de usuarios y clientes")
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Listar todos los roles", description = "Obtiene todos los roles registrados")
    @GetMapping
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @Operation(summary = "Listar roles activos", description = "Obtiene solo los roles con estado activo")
    @GetMapping("/active")
    public ResponseEntity<List<Role>> getAllActive() {
        return ResponseEntity.ok(roleService.findAllActive());
    }

    @Operation(summary = "Obtener rol por ID", description = "Busca un rol específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol encontrado"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Role> getById(
            @Parameter(description = "ID del rol", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @Operation(summary = "Crear nuevo rol", description = "Registra un nuevo rol en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rol creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Role> create(@Valid @RequestBody Role role) {
        Role created = roleService.create(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar rol", description = "Modifica los datos de un rol existente")
    @PutMapping("/{id}")
    public ResponseEntity<Role> update(
            @Parameter(description = "ID del rol", required = true) @PathVariable Integer id,
            @Valid @RequestBody Role role) {
        Role updated = roleService.update(id, role);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        roleService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol desactivado correctamente");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        roleService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol activado correctamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        roleService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rol eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Role> getByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.findByName(name));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Role>> getByType(@PathVariable RoleType type) {
        return ResponseEntity.ok(roleService.findByType(type));
    }

    @GetMapping("/employees/active")
    public ResponseEntity<List<Role>> getEmployeeRolesActive() {
        return ResponseEntity.ok(roleService.findEmployeeRolesActive());
    }

    @GetMapping("/customers/active")
    public ResponseEntity<List<Role>> getCustomerRolesActive() {
        return ResponseEntity.ok(roleService.findCustomerRolesActive());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Role>> search(@RequestParam String name) {
        return ResponseEntity.ok(roleService.searchByName(name));
    }

    @GetMapping("/can-place-orders")
    public ResponseEntity<List<Role>> getRolesCanPlaceOrders() {
        return ResponseEntity.ok(roleService.findRolesCanPlaceOrders());
    }

    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", roleService.countActive());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/type/{type}")
    public ResponseEntity<Map<String, Long>> countByType(@PathVariable RoleType type) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", roleService.countByType(type));
        return ResponseEntity.ok(response);
    }
}