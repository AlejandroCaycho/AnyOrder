package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.InventoryMovement;
import com.anyorder.pos.anyorder.service.InventoryMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory-movements")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Inventario", description = "Gestión de movimientos de inventario")
public class InventoryMovementController {

    private final InventoryMovementService movementService;

    @GetMapping
    @Operation(summary = "Listar todos los movimientos")
    public ResponseEntity<List<InventoryMovement>> getAll() {
        return ResponseEntity.ok(movementService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID")
    public ResponseEntity<InventoryMovement> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(movementService.findById(id));
    }

    @GetMapping("/ingredient/{ingredientId}")
    @Operation(summary = "Obtener historial de un ingrediente")
    public ResponseEntity<List<InventoryMovement>> getByIngredient(@PathVariable Integer ingredientId) {
        return ResponseEntity.ok(movementService.findByIngredientId(ingredientId));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Obtener movimientos por tipo")
    public ResponseEntity<List<InventoryMovement>> getByType(@PathVariable InventoryMovement.MovementType type) {
        return ResponseEntity.ok(movementService.findByMovementType(type));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener movimientos por usuario")
    public ResponseEntity<List<InventoryMovement>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(movementService.findByUserId(userId));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Obtener movimientos por rango de fechas")
    public ResponseEntity<List<InventoryMovement>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(movementService.findByDateRange(startDate, endDate));
    }

    @GetMapping("/ingredient/{ingredientId}/date-range")
    @Operation(summary = "Obtener movimientos de un ingrediente por rango de fechas")
    public ResponseEntity<List<InventoryMovement>> getByIngredientAndDateRange(
            @PathVariable Integer ingredientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(movementService.findByIngredientAndDateRange(ingredientId, startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo movimiento de inventario")
    public ResponseEntity<?> create(@Valid @RequestBody InventoryMovement movement) {
        try {
            InventoryMovement created = movementService.create(movement);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar movimiento de inventario")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody InventoryMovement movementData) { // SIN @Valid aquí
        try {
            InventoryMovement updated = movementService.update(id, movementData);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar movimiento y revertir stock")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        try {
            movementService.delete(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Movimiento eliminado y stock revertido correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}