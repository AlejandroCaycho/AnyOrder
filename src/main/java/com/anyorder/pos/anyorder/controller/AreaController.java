package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Area;
import com.anyorder.pos.anyorder.service.AreaService;
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
@RequestMapping("/api/areas")
@RequiredArgsConstructor
@Tag(name = "Áreas", description = "Gestión de áreas del restaurante")
public class AreaController {

    private final AreaService areaService;

    @Operation(summary = "Listar todas las áreas", description = "Obtiene todas las áreas registradas (activas e inactivas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Area>> getAll() {
        return ResponseEntity.ok(areaService.findAll());
    }

    @Operation(summary = "Listar áreas activas", description = "Obtiene solo las áreas con estado activo")
    @GetMapping("/active")
    public ResponseEntity<List<Area>> getAllActive() {
        return ResponseEntity.ok(areaService.findAllActive());
    }

    @Operation(summary = "Obtener área por ID", description = "Busca un área específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Área encontrada"),
            @ApiResponse(responseCode = "404", description = "Área no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Area> getById(
            @Parameter(description = "ID del área", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(areaService.findById(id));
    }

    @Operation(summary = "Crear nueva área", description = "Registra una nueva área en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Área creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Area> create(@Valid @RequestBody Area area) {
        Area created = areaService.create(area);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar área", description = "Modifica los datos de un área existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Área actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Área no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Area> update(
            @Parameter(description = "ID del área", required = true) @PathVariable Integer id,
            @Valid @RequestBody Area area) {
        Area updated = areaService.update(id, area);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        areaService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Área desactivada correctamente");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        areaService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Área activada correctamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        areaService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Área eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Area> getByName(@PathVariable String name) {
        return ResponseEntity.ok(areaService.findByName(name));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Area>> search(@RequestParam String name) {
        return ResponseEntity.ok(areaService.searchByName(name));
    }

    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", areaService.countActive());
        return ResponseEntity.ok(response);
    }
}