package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.InventoryMovement;
import com.anyorder.pos.anyorder.model.Ingredients;
import com.anyorder.pos.anyorder.repository.InventoryMovementRepository;
import com.anyorder.pos.anyorder.repository.IngredientsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryMovementService {

    private final InventoryMovementRepository movementRepository;
    private final IngredientsRepository ingredientsRepository;

    @Transactional(readOnly = true)
    public List<InventoryMovement> findAll() {
        log.debug("Obteniendo todos los movimientos de inventario");
        return movementRepository.findAll();
    }

    @Transactional(readOnly = true)
    public InventoryMovement findById(Integer id) {
        log.debug("Buscando movimiento con ID: {}", id);
        return movementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByIngredientId(Integer ingredientId) {
        log.debug("Obteniendo historial del ingrediente: {}", ingredientId);
        return movementRepository.findByIngredient_IdIngredient(ingredientId);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByMovementType(InventoryMovement.MovementType type) {
        log.debug("Obteniendo movimientos de tipo: {}", type);
        return movementRepository.findByMovementType(type);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByUserId(Integer userId) {
        log.debug("Obteniendo movimientos del usuario: {}", userId);
        return movementRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo movimientos entre {} y {}", startDate, endDate);
        return movementRepository.findMovementsByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<InventoryMovement> findByIngredientAndDateRange(Integer ingredientId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Obteniendo movimientos del ingrediente {} entre {} y {}", ingredientId, startDate, endDate);
        return movementRepository.findByIngredientAndDateRange(ingredientId, startDate, endDate);
    }

    @Transactional
    public InventoryMovement create(InventoryMovement movement) {
        log.info("Creando movimiento de inventario tipo: {}", movement.getMovementType());

        if (movement.getReason() == null || movement.getReason().trim().length() < 3) {
            throw new RuntimeException("La razón debe tener al menos 3 caracteres");
        }

        // Obtener ingrediente y su stock actual
        Ingredients ingredient = ingredientsRepository.findById(movement.getIngredient().getIdIngredient())
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

        // IMPORTANTE: Siempre usar el stock actual de la BD, ignorar lo que venga en el
        // request
        movement.setPreviousStock(ingredient.getQuantity());

        // Calcular el nuevo stock basado en el stock actual de la BD
        BigDecimal newStock = calculateNewStock(movement);
        movement.setNewStock(newStock);

        // Bloquear stock negativo
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El stock no puede ser negativo. Stock disponible: " + ingredient.getQuantity());
        }

        // Validar stock mínimo en salidas
        if (movement.getMovementType() == InventoryMovement.MovementType.SALIDA
                || movement.getMovementType() == InventoryMovement.MovementType.MERMA) {
            if (newStock.compareTo(ingredient.getMinStock()) < 0) {
                log.warn("ADVERTENCIA: Stock caerá por debajo del mínimo para ingrediente {}",
                        ingredient.getIdIngredient());
            }
        }

        // Calcular costo total si se proporciona unitCost
        if (movement.getUnitCost() != null && movement.getUnitCost().compareTo(BigDecimal.ZERO) > 0) {
            movement.setTotalCost(movement.getQuantity().multiply(movement.getUnitCost()));
        }

        InventoryMovement saved = movementRepository.save(movement);
        log.info("Movimiento creado con ID: {}", saved.getIdMovement());

        // Actualizar stock del ingrediente
        ingredient.setQuantity(newStock);
        ingredientsRepository.save(ingredient);
        log.info("Stock actualizado para ingrediente {} de {} a {}", ingredient.getIdIngredient(),
                movement.getPreviousStock(), newStock);

        return saved;
    }

    @Transactional
    public InventoryMovement update(Integer id, InventoryMovement movementData) {
        log.info("Actualizando movimiento con ID: {}", id);

        InventoryMovement movement = findById(id);
        Ingredients ingredient = movement.getIngredient();

        // Revertir el stock anterior
        ingredient.setQuantity(movement.getPreviousStock());

        // Actualizar datos
        movement.setMovementType(movementData.getMovementType());
        movement.setQuantity(movementData.getQuantity());
        movement.setReason(movementData.getReason());
        movement.setNotes(movementData.getNotes());
        movement.setUnitCost(movementData.getUnitCost());

        // Recalcular newStock
        BigDecimal newStock = calculateNewStock(movement);
        movement.setNewStock(newStock);

        // Validaciones
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }

        if (movement.getUnitCost() != null && movement.getUnitCost().compareTo(BigDecimal.ZERO) > 0) {
            movement.setTotalCost(movement.getQuantity().multiply(movement.getUnitCost()));
        }

        InventoryMovement updated = movementRepository.save(movement);
        log.info("Movimiento actualizado con ID: {}", id);

        // Actualizar ingrediente
        ingredient.setQuantity(newStock);
        ingredientsRepository.save(ingredient);

        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando movimiento con ID: {}", id);

        InventoryMovement movement = findById(id);
        Ingredients ingredient = movement.getIngredient();

        // Revertir cambios de stock
        ingredient.setQuantity(movement.getPreviousStock());
        ingredientsRepository.save(ingredient);

        movementRepository.deleteById(id);
        log.info("Movimiento eliminado y stock revertido");
    }

    private BigDecimal calculateNewStock(InventoryMovement movement) {
        BigDecimal previous = movement.getPreviousStock();
        BigDecimal qty = movement.getQuantity();

        return switch (movement.getMovementType()) {
            case ENTRADA -> previous.add(qty);
            case SALIDA, MERMA -> previous.subtract(qty);
            case DEVOLUCION -> previous.add(qty);
            case AJUSTE -> qty; // El ajuste reemplaza el stock
        };
    }
}