package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {

    List<InventoryMovement> findByIngredient_IdIngredient(Integer ingredientId);

    List<InventoryMovement> findByMovementType(InventoryMovement.MovementType type);

    List<InventoryMovement> findByUser_IdUser(Integer userId);

    @Query("SELECT im FROM InventoryMovement im WHERE im.movementDate BETWEEN :startDate AND :endDate ORDER BY im.movementDate DESC")
    List<InventoryMovement> findMovementsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT im FROM InventoryMovement im WHERE im.ingredient.idIngredient = :ingredientId AND im.movementDate BETWEEN :startDate AND :endDate ORDER BY im.movementDate DESC")
    List<InventoryMovement> findByIngredientAndDateRange(
            @Param("ingredientId") Integer ingredientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT im FROM InventoryMovement im WHERE im.movementType = :type AND im.ingredient.idIngredient = :ingredientId")
    List<InventoryMovement> findByIngredientAndMovementType(
            @Param("ingredientId") Integer ingredientId,
            @Param("type") InventoryMovement.MovementType type);

    @Query("SELECT COUNT(im) FROM InventoryMovement im WHERE im.movementType = :type")
    long countByMovementType(@Param("type") InventoryMovement.MovementType type);
}