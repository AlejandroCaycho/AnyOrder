package com.anyorder.pos.anyorder.modules.ingredients.service;

import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.ingredients.repository.IngredientRepository;
import com.anyorder.pos.anyorder.modules.suppliers.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class IngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;

    public List<Ingredient> findAll() {
        return ingredientRepository.findAll();
    }

    public List<Ingredient> findAllActive() {
        return ingredientRepository.findByStateTrue();
    }

    public Optional<Ingredient> findById(Integer id) {
        return ingredientRepository.findById(id);
    }
    
    public Optional<Ingredient> findByCode(String code) {
        return ingredientRepository.findByCode(code);
    }

    @Transactional
    public Ingredient create(Ingredient ingredient) {
        if (ingredientRepository.existsByCode(ingredient.getCode())) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
        }
        if (!supplierRepository.existsById(ingredient.getIdSupplier())) {
            throw new IllegalArgumentException("El proveedor con ID " + ingredient.getIdSupplier() + " no existe");
        }
        if (ingredient.getState() == null) ingredient.setState(true);
        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public Ingredient update(Integer id, Ingredient details) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con ID: " + id));
        
        if (!ingredient.getCode().equals(details.getCode()) && ingredientRepository.existsByCode(details.getCode())) {
            throw new IllegalArgumentException("Ya existe un ingrediente con ese código");
        }
        
        if (!supplierRepository.existsById(details.getIdSupplier())) {
            throw new IllegalArgumentException("El proveedor con ID " + details.getIdSupplier() + " no existe");
        }

        ingredient.setName(details.getName());
        ingredient.setCode(details.getCode());
        ingredient.setCategory(details.getCategory());
        ingredient.setUnit(details.getUnit());
        ingredient.setQuantity(details.getQuantity());
        ingredient.setMinStock(details.getMinStock());
        ingredient.setUnitPrice(details.getUnitPrice());
        ingredient.setIdSupplier(details.getIdSupplier());
        ingredient.setExpirationDate(details.getExpirationDate());
        ingredient.setDescription(details.getDescription());
        ingredient.setState(details.getState());

        return ingredientRepository.save(ingredient);
    }

    @Transactional
    public void delete(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con ID: " + id));
        ingredient.setState(false);
        ingredientRepository.save(ingredient);
    }
    
    @Transactional
    public void deletePermanently(Integer id) {
        if (!ingredientRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente no encontrado con ID: " + id);
        }
        ingredientRepository.deleteById(id);
    }
    
    @Transactional
    public Ingredient activate(Integer id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con ID: " + id));
        ingredient.setState(true);
        return ingredientRepository.save(ingredient);
    }
}
