package com.anyorder.pos.anyorder.modules.presentations_ingredients.repository;

import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredient;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresentationIngredientRepository extends JpaRepository<PresentationIngredient, PresentationIngredientId> {
    
    @EntityGraph(attributePaths = {"presentation", "ingredient"})
    List<PresentationIngredient> findById_IdPresentation(Integer idPresentation);
    
    @EntityGraph(attributePaths = {"presentation", "ingredient"})
    List<PresentationIngredient> findById_IdIngredient(Integer idIngredient);
}
