package com.anyorder.pos.anyorder.modules.inventory.service;

import com.anyorder.pos.anyorder.modules.inventory.model.InventoryMovement;
import com.anyorder.pos.anyorder.modules.inventory.repository.InventoryMovementRepository;
import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.ingredients.repository.IngredientRepository;
import com.anyorder.pos.anyorder.modules.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryMovementRepository movementRepository;
    private final IngredientRepository ingredientRepository;

    @Transactional(readOnly = true)
    public List<InventoryMovement> findAll() {
        return movementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByIngredient(Integer ingredientId) {
        return movementRepository.findByIngredient_IdIngredientOrderByMovementDateDesc(ingredientId);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByType(InventoryMovement.MovementType type) {
        return movementRepository.findByMovementType(type);
    }

    /**
     * Registers a manual inventory movement (ENTRADA/AJUSTE/MERMA/DEVOLUCION).
     * Stock update is done here; SALIDA is not allowed manually (it's internal, triggered by sales).
     */
    @Transactional
    public InventoryMovement registerManualMovement(InventoryMovement movement) {
        if (movement.getMovementType() == InventoryMovement.MovementType.SALIDA) {
            throw new IllegalArgumentException("Las salidas de inventario son automáticas. Use el flujo de ventas.");
        }

        Ingredient ingredient = ingredientRepository.findById(movement.getIngredient().getIdIngredient())
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        BigDecimal previousStock = ingredient.getQuantity();
        BigDecimal newStock;

        switch (movement.getMovementType()) {
            case ENTRADA, DEVOLUCION -> newStock = previousStock.add(movement.getQuantity());
            case MERMA, AJUSTE -> {
                if (previousStock.compareTo(movement.getQuantity()) < 0) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente. Disponible: " + previousStock + ", Requerido: " + movement.getQuantity());
                }
                newStock = previousStock.subtract(movement.getQuantity());
            }
            default -> throw new IllegalArgumentException("Tipo de movimiento no válido: " + movement.getMovementType());
        }

        ingredient.setQuantity(newStock);
        ingredientRepository.save(ingredient);

        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);

        if (movement.getUnitCost() != null) {
            movement.setTotalCost(movement.getUnitCost().multiply(movement.getQuantity()));
        }

        log.info("Movimiento {} registrado: ingrediente={}, prevStock={}, newStock={}",
                movement.getMovementType(), ingredient.getName(), previousStock, newStock);

        return movementRepository.save(movement);
    }

    /**
     * Internal: registers a SALIDA movement when an order is confirmed.
     * Called by OrderService.confirmOrder() — NOT exposed via REST directly.
     */
    @Transactional
    public InventoryMovement registerSalida(Ingredient ingredient, BigDecimal quantity, User user, String reason) {
        BigDecimal previousStock = ingredient.getQuantity();

        if (previousStock.compareTo(quantity) < 0) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para " + ingredient.getName() +
                    ". Disponible: " + previousStock + ", Requerido: " + quantity);
        }

        BigDecimal newStock = previousStock.subtract(quantity);
        ingredient.setQuantity(newStock);
        ingredientRepository.save(ingredient);

        InventoryMovement movement = new InventoryMovement();
        movement.setIngredient(ingredient);
        movement.setMovementType(InventoryMovement.MovementType.SALIDA);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(reason);
        movement.setUser(user);

        return movementRepository.save(movement);
    }

    /**
     * Internal: restores stock (DEVOLUCION) when a confirmed order is cancelled.
     */
    @Transactional
    public InventoryMovement registerDevolucion(Ingredient ingredient, BigDecimal quantity, User user, String reason) {
        BigDecimal previousStock = ingredient.getQuantity();
        BigDecimal newStock = previousStock.add(quantity);

        ingredient.setQuantity(newStock);
        ingredientRepository.save(ingredient);

        InventoryMovement movement = new InventoryMovement();
        movement.setIngredient(ingredient);
        movement.setMovementType(InventoryMovement.MovementType.DEVOLUCION);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReason(reason);
        movement.setUser(user);

        return movementRepository.save(movement);
    }
}
