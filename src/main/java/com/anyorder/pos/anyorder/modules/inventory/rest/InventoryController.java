package com.anyorder.pos.anyorder.modules.inventory.rest;

import com.anyorder.pos.anyorder.modules.inventory.model.InventoryMovement;
import com.anyorder.pos.anyorder.modules.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/movements")
    public ResponseEntity<List<InventoryMovement>> getAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/movements/ingredient/{ingredientId}")
    public ResponseEntity<List<InventoryMovement>> getByIngredient(@PathVariable Integer ingredientId) {
        return ResponseEntity.ok(inventoryService.findByIngredient(ingredientId));
    }

    @GetMapping("/movements/type/{type}")
    public ResponseEntity<List<InventoryMovement>> getByType(@PathVariable InventoryMovement.MovementType type) {
        return ResponseEntity.ok(inventoryService.findByType(type));
    }

    /** Manual movements: ENTRADA, AJUSTE, MERMA, DEVOLUCION */
    @PostMapping("/movements")
    public ResponseEntity<InventoryMovement> register(@Valid @RequestBody InventoryMovement movement) {
        return ResponseEntity.status(201).body(inventoryService.registerManualMovement(movement));
    }
}
