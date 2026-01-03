package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Ingredients;
import com.anyorder.pos.anyorder.service.IngredientsService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Tag(name = "Ingredientes", description = "Gestión de ingredientes del inventario")
public class IngredientsController {

    private final IngredientsService ingredientsService;

    @Operation(summary = "Listar todos los ingredientes", description = "Obtiene todos los ingredientes registrados (activos e inactivos)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Ingredients>> getAll() {
        return ResponseEntity.ok(ingredientsService.findAll());
    }

    @Operation(summary = "Listar ingredientes activos", description = "Obtiene solo los ingredientes con estado activo")
    @GetMapping("/active")
    public ResponseEntity<List<Ingredients>> getAllActive() {
        return ResponseEntity.ok(ingredientsService.findAllActive());
    }

    @Operation(summary = "Obtener ingrediente por ID", description = "Busca un ingrediente específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingrediente encontrado"),
            @ApiResponse(responseCode = "404", description = "Ingrediente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Ingredients> getById(
            @Parameter(description = "ID del ingrediente", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(ingredientsService.findById(id));
    }

    @Operation(summary = "Crear nuevo ingrediente", description = "Registra un nuevo ingrediente en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ingrediente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<Ingredients> create(@Valid @RequestBody Ingredients ingredients) {
        Ingredients created = ingredientsService.create(ingredients);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar ingrediente", description = "Modifica los datos de un ingrediente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingrediente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Ingrediente no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Ingredients> update(
            @Parameter(description = "ID del ingrediente", required = true) @PathVariable Integer id,
            @Valid @RequestBody Ingredients ingredients) {
        Ingredients updated = ingredientsService.update(id, ingredients);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Desactivar ingrediente")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        ingredientsService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ingrediente desactivado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activar ingrediente")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        ingredientsService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ingrediente activado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar ingrediente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        ingredientsService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ingrediente eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener ingrediente por código")
    @GetMapping("/code/{code}")
    public ResponseEntity<Ingredients> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ingredientsService.findByCode(code));
    }

    @Operation(summary = "Obtener ingredientes por categoría")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Ingredients>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ingredientsService.findByCategory(category));
    }

    @Operation(summary = "Obtener ingredientes activos por categoría")
    @GetMapping("/category/{category}/active")
    public ResponseEntity<List<Ingredients>> getActiveByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ingredientsService.findActiveIngredientsByCategory(category));
    }

    @Operation(summary = "Obtener ingredientes por proveedor")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<Ingredients>> getBySupplier(@PathVariable Integer supplierId) {
        return ResponseEntity.ok(ingredientsService.findBySupplier(supplierId));
    }

    @Operation(summary = "Obtener ingredientes activos por proveedor")
    @GetMapping("/supplier/{supplierId}/active")
    public ResponseEntity<List<Ingredients>> getActiveBySupplier(@PathVariable Integer supplierId) {
        return ResponseEntity.ok(ingredientsService.findActiveIngredientsBySupplier(supplierId));
    }

    @Operation(summary = "Buscar ingredientes por nombre")
    @GetMapping("/search")
    public ResponseEntity<List<Ingredients>> search(@RequestParam String name) {
        return ResponseEntity.ok(ingredientsService.searchByName(name));
    }

    @Operation(summary = "Buscar ingredientes por categoría (búsqueda)")
    @GetMapping("/search-category")
    public ResponseEntity<List<Ingredients>> searchByCategory(@RequestParam String category) {
        return ResponseEntity.ok(ingredientsService.searchByCategory(category));
    }

    @Operation(summary = "Obtener ingredientes con stock bajo", description = "Ingredientes cuya cantidad es menor o igual al stock mínimo")
    @GetMapping("/low-stock")
    public ResponseEntity<List<Ingredients>> getLowStock() {
        return ResponseEntity.ok(ingredientsService.findLowStockIngredients());
    }

    @Operation(summary = "Obtener ingredientes vencidos")
    @GetMapping("/expired")
    public ResponseEntity<List<Ingredients>> getExpired() {
        return ResponseEntity.ok(ingredientsService.findExpiredIngredients());
    }

    @Operation(summary = "Obtener ingredientes a vencer en próxima semana")
    @GetMapping("/expiring-next-week")
    public ResponseEntity<List<Ingredients>> getExpiringNextWeek() {
        return ResponseEntity.ok(ingredientsService.findExpiringIngredientsNextWeek());
    }

    @Operation(summary = "Actualizar cantidad de ingrediente")
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Ingredients> updateQuantity(
            @PathVariable Integer id,
            @RequestParam BigDecimal quantity) {
        return ResponseEntity.ok(ingredientsService.updateQuantity(id, quantity));
    }

    @Operation(summary = "Ajustar cantidad de ingrediente", description = "Suma o resta a la cantidad actual")
    @PatchMapping("/{id}/adjust-quantity")
    public ResponseEntity<Ingredients> adjustQuantity(
            @PathVariable Integer id,
            @RequestParam BigDecimal adjustment) {
        return ResponseEntity.ok(ingredientsService.adjustQuantity(id, adjustment));
    }

    @Operation(summary = "Obtener todas las categorías activas")
    @GetMapping("/categories/all")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(ingredientsService.getAllActiveCategories());
    }

    @Operation(summary = "Obtener todas las unidades de medida activas")
    @GetMapping("/units/all")
    public ResponseEntity<List<String>> getAllUnits() {
        return ResponseEntity.ok(ingredientsService.getAllActiveUnits());
    }

    @Operation(summary = "Contar ingredientes activos")
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", ingredientsService.countActive());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar ingredientes con stock bajo")
    @GetMapping("/count/low-stock")
    public ResponseEntity<Map<String, Long>> countLowStock() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", ingredientsService.countLowStock());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar ingredientes vencidos")
    @GetMapping("/count/expired")
    public ResponseEntity<Map<String, Long>> countExpired() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", ingredientsService.countExpired());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar ingredientes a vencer próxima semana")
    @GetMapping("/count/expiring-next-week")
    public ResponseEntity<Map<String, Long>> countExpiringNextWeek() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", ingredientsService.countExpiringNextWeek());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar ingredientes por proveedor")
    @GetMapping("/count/supplier/{supplierId}")
    public ResponseEntity<Map<String, Long>> countBySupplier(@PathVariable Integer supplierId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", ingredientsService.countBySupplier(supplierId));
        return ResponseEntity.ok(response);
    }
}