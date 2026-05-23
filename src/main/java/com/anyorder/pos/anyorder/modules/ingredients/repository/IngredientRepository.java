package com.anyorder.pos.anyorder.modules.ingredients.repository;

import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {
    
    @EntityGraph(attributePaths = {"supplier"})
    List<Ingredient> findAll();

    @EntityGraph(attributePaths = {"supplier"})
    Optional<Ingredient> findByCode(String code);

    boolean existsByCode(String code);
    
    boolean existsByName(String name);
    
    @EntityGraph(attributePaths = {"supplier"})
    List<Ingredient> findByStateTrue();
    
    @EntityGraph(attributePaths = {"supplier"})
    List<Ingredient> findBySupplier_IdSupplier(Integer idSupplier);
}
