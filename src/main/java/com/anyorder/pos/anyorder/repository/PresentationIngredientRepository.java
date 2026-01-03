package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.PresentationIngredient;
import com.anyorder.pos.anyorder.model.PresentationIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PresentationIngredientRepository
        extends JpaRepository<PresentationIngredient, PresentationIngredientId> {

    // Ingredientes de una presentación
    @Query("SELECT pi FROM PresentationIngredient pi WHERE pi.idPresentation = :presentationId")
    List<PresentationIngredient> findByPresentationId(@Param("presentationId") Integer presentationId);

    // Presentaciones que usan un ingrediente
    @Query("SELECT pi FROM PresentationIngredient pi WHERE pi.idIngredient = :ingredientId")
    List<PresentationIngredient> findByIngredientId(@Param("ingredientId") Integer ingredientId);

    // Eliminar todos los ingredientes de una presentación
    void deleteByIdPresentation(Integer presentationId);

    // Eliminar un ingrediente específico de una presentación
    void deleteByIdPresentationAndIdIngredient(Integer presentationId, Integer ingredientId);

    // Contar ingredientes de una presentación
    long countByIdPresentation(Integer presentationId);

    // Verificar si existe la relación
    boolean existsByIdPresentationAndIdIngredient(Integer presentationId, Integer ingredientId);

    // Suma total del costo de ingredientes (calculado)
    @Query("SELECT SUM(pi.quantity * pi.ingredient.unitPrice) FROM PresentationIngredient pi WHERE pi.idPresentation = :presentationId")
    BigDecimal sumIngredientCostByPresentation(@Param("presentationId") Integer presentationId);
}