package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.PresentationIngredient;
import com.anyorder.pos.anyorder.service.PresentationIngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presentation-ingredients")
@RequiredArgsConstructor
@Tag(name = "Presentation Ingredients", description = "Gestión de ingredientes por presentación (recetas)")
public class PresentationIngredientController {

    private final PresentationIngredientService presentationIngredientService;

    @Operation(summary = "Listar todas las relaciones")
    @GetMapping
    public ResponseEntity<List<PresentationIngredient>> getAll() {
        return ResponseEntity.ok(presentationIngredientService.findAll());
    }

    @Operation(summary = "Ingredientes de una presentación", description = "Obtiene la receta de un plato")
    @GetMapping("/presentation/{presentationId}")
    public ResponseEntity<List<PresentationIngredient>> getByPresentation(@PathVariable Integer presentationId) {
        return ResponseEntity.ok(presentationIngredientService.findByPresentationId(presentationId));
    }

    @Operation(summary = "Presentaciones que usan un ingrediente", description = "Ver en qué platos se usa un ingrediente")
    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<PresentationIngredient>> getByIngredient(@PathVariable Integer ingredientId) {
        return ResponseEntity.ok(presentationIngredientService.findByIngredientId(ingredientId));
    }

    @Operation(summary = "Agregar ingrediente a presentación")
    @PostMapping
    public ResponseEntity<?> addIngredient(@RequestBody Map<String, Object> request) {
        try {
            Integer presentationId = (Integer) request.get("presentationId");
            Integer ingredientId = (Integer) request.get("ingredientId");
            BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
            String unit = (String) request.get("unit");
            String notes = (String) request.get("notes");

            PresentationIngredient created = presentationIngredientService.addIngredientToPresentation(
                    presentationId, ingredientId, quantity, unit, notes);

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar cantidad de ingrediente")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> request) {
        try {
            Integer presentationId = (Integer) request.get("presentationId");
            Integer ingredientId = (Integer) request.get("ingredientId");
            BigDecimal quantity = new BigDecimal(request.get("quantity").toString());
            String unit = (String) request.get("unit");

            PresentationIngredient updated = presentationIngredientService.updateQuantity(
                    presentationId, ingredientId, quantity, unit);

            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Remover ingrediente de presentación")
    @DeleteMapping("/presentation/{presentationId}/ingredient/{ingredientId}")
    public ResponseEntity<?> removeIngredient(
            @PathVariable Integer presentationId,
            @PathVariable Integer ingredientId) {
        try {
            presentationIngredientService.removeIngredientFromPresentation(presentationId, ingredientId);
            return ResponseEntity.ok(Map.of("message", "Ingrediente removido exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Remover todos los ingredientes de una presentación")
    @DeleteMapping("/presentation/{presentationId}/all")
    public ResponseEntity<?> removeAllIngredients(@PathVariable Integer presentationId) {
        try {
            presentationIngredientService.removeAllIngredientsFromPresentation(presentationId);
            return ResponseEntity.ok(Map.of("message", "Todos los ingredientes removidos"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Calcular costo de ingredientes", description = "Suma el costo de todos los ingredientes de un plato")
    @GetMapping("/presentation/{presentationId}/cost")
    public ResponseEntity<Map<String, BigDecimal>> getTotalCost(@PathVariable Integer presentationId) {
        BigDecimal cost = presentationIngredientService.calculateTotalIngredientCost(presentationId);
        return ResponseEntity.ok(Map.of("totalCost", cost));
    }

    @Operation(summary = "Verificar stock suficiente", description = "Verifica si hay stock para preparar N porciones")
    @GetMapping("/presentation/{presentationId}/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable Integer presentationId,
            @RequestParam(defaultValue = "1") Integer portions) {
        boolean hasStock = presentationIngredientService.hasEnoughStockForPresentation(presentationId, portions);
        Map<String, Object> response = new HashMap<>();
        response.put("hasStock", hasStock);
        response.put("portions", portions);
        response.put("message", hasStock ? "Stock suficiente" : "Stock insuficiente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar ingredientes de presentación")
    @GetMapping("/presentation/{presentationId}/count")
    public ResponseEntity<Map<String, Long>> countIngredients(@PathVariable Integer presentationId) {
        return ResponseEntity.ok(Map.of("total", presentationIngredientService.countByPresentation(presentationId)));
    }
}