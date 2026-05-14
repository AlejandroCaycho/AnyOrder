package com.anyorder.pos.anyorder.modules.inventory.repository;

import com.anyorder.pos.anyorder.modules.inventory.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {

    List<InventoryMovement> findByIngredient_IdIngredientOrderByMovementDateDesc(Integer idIngredient);

    List<InventoryMovement> findByMovementType(InventoryMovement.MovementType type);

    List<InventoryMovement> findByUser_IdUser(Integer idUser);

    @Query("SELECT m FROM InventoryMovement m WHERE m.movementDate BETWEEN :start AND :end ORDER BY m.movementDate DESC")
    List<InventoryMovement> findByDateRange(LocalDateTime start, LocalDateTime end);
}
