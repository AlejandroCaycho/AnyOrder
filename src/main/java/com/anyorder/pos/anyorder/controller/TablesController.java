package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Tables;
import com.anyorder.pos.anyorder.service.TablesService;
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
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Tag(name = "Mesas", description = "Gestión de mesas del restaurante")
public class TablesController {

    private final TablesService tablesService;

    @Operation(summary = "Listar todas las mesas", description = "Obtiene todas las mesas registradas (activas e inactivas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Tables>> getAll() {
        return ResponseEntity.ok(tablesService.findAll());
    }

    @Operation(summary = "Listar mesas activas", description = "Obtiene solo las mesas con estado activo")
    @GetMapping("/active")
    public ResponseEntity<List<Tables>> getAllActive() {
        return ResponseEntity.ok(tablesService.findAllActive());
    }

    @Operation(summary = "Obtener mesa por ID", description = "Busca una mesa específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa encontrada"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Tables> getById(
            @Parameter(description = "ID de la mesa", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(tablesService.findById(id));
    }

    @Operation(summary = "Crear nueva mesa", description = "Registra una nueva mesa en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Mesa creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Tables> create(@Valid @RequestBody Tables tables) {
        Tables created = tablesService.create(tables);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar mesa", description = "Modifica los datos de una mesa existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mesa actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Mesa no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Tables> update(
            @Parameter(description = "ID de la mesa", required = true) @PathVariable Integer id,
            @Valid @RequestBody Tables tables) {
        Tables updated = tablesService.update(id, tables);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Desactivar mesa")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        tablesService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mesa desactivada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activar mesa")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        tablesService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mesa activada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar mesa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        tablesService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Mesa eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener mesa por nombre")
    @GetMapping("/name/{name}")
    public ResponseEntity<Tables> getByName(@PathVariable String name) {
        return ResponseEntity.ok(tablesService.findByName(name));
    }

    @Operation(summary = "Obtener mesa por QR Token")
    @GetMapping("/qr/{qrToken}")
    public ResponseEntity<Tables> getByQrToken(@PathVariable String qrToken) {
        return ResponseEntity.ok(tablesService.findByQrToken(qrToken));
    }

    @Operation(summary = "Obtener mesas por área")
    @GetMapping("/area/{areaId}")
    public ResponseEntity<List<Tables>> getByArea(@PathVariable Integer areaId) {
        return ResponseEntity.ok(tablesService.findByArea(areaId));
    }

    @Operation(summary = "Obtener mesas activas por área")
    @GetMapping("/area/{areaId}/active")
    public ResponseEntity<List<Tables>> getActiveByArea(@PathVariable Integer areaId) {
        return ResponseEntity.ok(tablesService.findActiveByArea(areaId));
    }

    @Operation(summary = "Obtener mesas disponibles", description = "Mesas con capacidad disponible")
    @GetMapping("/available")
    public ResponseEntity<List<Tables>> getAvailableTables() {
        return ResponseEntity.ok(tablesService.findAvailableTables());
    }

    @Operation(summary = "Obtener mesas disponibles por área")
    @GetMapping("/available/area/{areaId}")
    public ResponseEntity<List<Tables>> getAvailableTablesByArea(@PathVariable Integer areaId) {
        return ResponseEntity.ok(tablesService.findAvailableTablesByArea(areaId));
    }

    @Operation(summary = "Obtener mesas ocupadas")
    @GetMapping("/occupied")
    public ResponseEntity<List<Tables>> getOccupiedTables() {
        return ResponseEntity.ok(tablesService.findOccupiedTables());
    }

    @Operation(summary = "Buscar mesas por nombre")
    @GetMapping("/search")
    public ResponseEntity<List<Tables>> search(@RequestParam String name) {
        return ResponseEntity.ok(tablesService.searchByName(name));
    }

    @Operation(summary = "Buscar mesas por ubicación")
    @GetMapping("/search-location")
    public ResponseEntity<List<Tables>> searchByLocation(@RequestParam String location) {
        return ResponseEntity.ok(tablesService.searchByLocation(location));
    }

    @Operation(summary = "Ocupar mesa", description = "Marca una mesa como ocupada y establece el número de personas")
    @PatchMapping("/{id}/occupy")
    public ResponseEntity<Tables> occupyTable(
            @PathVariable Integer id,
            @RequestParam Integer numberOfPeople) {
        return ResponseEntity.ok(tablesService.occupyTable(id, numberOfPeople));
    }

    @Operation(summary = "Liberar mesa", description = "Marca una mesa como disponible")
    @PatchMapping("/{id}/release")
    public ResponseEntity<Tables> releaseTable(@PathVariable Integer id) {
        return ResponseEntity.ok(tablesService.releaseTable(id));
    }

    @Operation(summary = "Actualizar ocupancia", description = "Actualiza el número de personas en una mesa ocupada")
    @PatchMapping("/{id}/update-occupancy")
    public ResponseEntity<Tables> updateOccupancy(
            @PathVariable Integer id,
            @RequestParam Integer occupancy) {
        return ResponseEntity.ok(tablesService.updateOccupancy(id, occupancy));
    }

    @Operation(summary = "Contar mesas activas")
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", tablesService.countActive());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar mesas ocupadas")
    @GetMapping("/count/occupied")
    public ResponseEntity<Map<String, Long>> countOccupied() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", tablesService.countOccupied());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar mesas disponibles")
    @GetMapping("/count/available")
    public ResponseEntity<Map<String, Long>> countAvailable() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", tablesService.countAvailable());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar mesas por área")
    @GetMapping("/count/area/{areaId}")
    public ResponseEntity<Map<String, Long>> countByArea(@PathVariable Integer areaId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", tablesService.countByArea(areaId));
        return ResponseEntity.ok(response);
    }
}