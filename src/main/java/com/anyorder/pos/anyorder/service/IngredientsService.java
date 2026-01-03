package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Ingredients;
import com.anyorder.pos.anyorder.repository.IngredientsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientsService {

    private final IngredientsRepository ingredientsRepository;
    private final SupplierService supplierService;

    @Transactional(readOnly = true)
    public List<Ingredients> findAll() {
        log.debug("Obteniendo todos los ingredientes");
        return ingredientsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findAllActive() {
        log.debug("Obteniendo ingredientes activos");
        return ingredientsRepository.findByStateTrue();
    }

    @Transactional(readOnly = true)
    public Ingredients findById(Integer id) {
        log.debug("Buscando ingrediente con ID: {}", id);
        return ingredientsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con ID: " + id));
    }

    @Transactional
    public Ingredients create(Ingredients ingredients) {
        log.info("Creando nuevo ingrediente: {}", ingredients.getName());

        // Validar código único
        if (ingredientsRepository.existsByCode(ingredients.getCode())) {
            throw new RuntimeException("Ya existe un ingrediente con el código: " + ingredients.getCode());
        }

        // Validar nombre
        if (ingredients.getName() == null || ingredients.getName().trim().length() < 2) {
            throw new RuntimeException("El nombre debe tener al menos 2 caracteres");
        }

        // Validar código
        if (ingredients.getCode() == null || ingredients.getCode().trim().length() < 2) {
            throw new RuntimeException("El código debe tener al menos 2 caracteres");
        }

        // Validar categoría
        if (ingredients.getCategory() == null || ingredients.getCategory().trim().length() < 2) {
            throw new RuntimeException("La categoría debe tener al menos 2 caracteres");
        }

        // Validar unidad
        if (ingredients.getUnit() == null || ingredients.getUnit().trim().length() < 1) {
            throw new RuntimeException("La unidad es obligatoria");
        }

        // Validar proveedor existe
        if (ingredients.getSupplier() == null || ingredients.getSupplier().getIdSupplier() == null) {
            throw new RuntimeException("El proveedor es obligatorio");
        }
        supplierService.findById(ingredients.getSupplier().getIdSupplier());

        // Inicializar valores por defecto
        if (ingredients.getState() == null) {
            ingredients.setState(true);
        }
        if (ingredients.getQuantity() == null) {
            ingredients.setQuantity(BigDecimal.ZERO);
        }
        if (ingredients.getMinStock() == null) {
            ingredients.setMinStock(BigDecimal.ZERO);
        }
        if (ingredients.getUnitPrice() == null) {
            ingredients.setUnitPrice(BigDecimal.ZERO);
        }

        Ingredients savedIngredients = ingredientsRepository.save(ingredients);
        log.info("Ingrediente creado exitosamente con ID: {}", savedIngredients.getIdIngredient());

        return savedIngredients;
    }

    @Transactional
    public Ingredients update(Integer id, Ingredients ingredientsData) {
        log.info("Actualizando ingrediente con ID: {}", id);

        Ingredients ingredients = findById(id);

        // Validar código único (si cambió)
        if (!ingredients.getCode().equals(ingredientsData.getCode())) {
            if (ingredientsRepository.existsByCode(ingredientsData.getCode())) {
                throw new RuntimeException("Ya existe un ingrediente con el código: " + ingredientsData.getCode());
            }
        }

        // Validar proveedor existe (si cambió)
        if (ingredientsData.getSupplier() != null &&
                !ingredients.getSupplier().getIdSupplier().equals(ingredientsData.getSupplier().getIdSupplier())) {
            supplierService.findById(ingredientsData.getSupplier().getIdSupplier());
        }

        // Actualizar campos
        ingredients.setName(ingredientsData.getName());
        ingredients.setCode(ingredientsData.getCode());
        ingredients.setCategory(ingredientsData.getCategory());
        ingredients.setUnit(ingredientsData.getUnit());

        if (ingredientsData.getQuantity() != null) {
            ingredients.setQuantity(ingredientsData.getQuantity());
        }
        if (ingredientsData.getMinStock() != null) {
            ingredients.setMinStock(ingredientsData.getMinStock());
        }
        if (ingredientsData.getUnitPrice() != null) {
            ingredients.setUnitPrice(ingredientsData.getUnitPrice());
        }

        if (ingredientsData.getSupplier() != null) {
            ingredients.setSupplier(ingredientsData.getSupplier());
        }

        if (ingredientsData.getExpirationDate() != null) {
            ingredients.setExpirationDate(ingredientsData.getExpirationDate());
        }

        if (ingredientsData.getDescription() != null) {
            ingredients.setDescription(ingredientsData.getDescription());
        }

        if (ingredientsData.getState() != null) {
            ingredients.setState(ingredientsData.getState());
        }

        Ingredients updatedIngredients = ingredientsRepository.save(ingredients);
        log.info("Ingrediente actualizado exitosamente con ID: {}", updatedIngredients.getIdIngredient());

        return updatedIngredients;
    }

    @Transactional
    public void deactivate(Integer id) {
        log.info("Desactivando ingrediente con ID: {}", id);
        Ingredients ingredients = findById(id);
        ingredients.setState(false);
        ingredientsRepository.save(ingredients);
        log.info("Ingrediente {} desactivado exitosamente", id);
    }

    @Transactional
    public void activate(Integer id) {
        log.info("Activando ingrediente con ID: {}", id);
        Ingredients ingredients = findById(id);
        ingredients.setState(true);
        ingredientsRepository.save(ingredients);
        log.info("Ingrediente {} activado exitosamente", id);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando ingrediente con ID: {}", id);
        if (!ingredientsRepository.existsById(id)) {
            throw new RuntimeException("Ingrediente no encontrado con ID: " + id);
        }
        ingredientsRepository.deleteById(id);
        log.info("Ingrediente {} eliminado exitosamente", id);
    }

    @Transactional(readOnly = true)
    public Ingredients findByCode(String code) {
        log.debug("Buscando ingrediente por código: {}", code);
        return ingredientsRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con código: " + code));
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findByCategory(String category) {
        log.debug("Obteniendo ingredientes de categoría: {}", category);
        return ingredientsRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findBySupplier(Integer supplierId) {
        log.debug("Obteniendo ingredientes del proveedor: {}", supplierId);
        supplierService.findById(supplierId);
        return ingredientsRepository.findBySupplier_IdSupplier(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findActiveIngredientsBySupplier(Integer supplierId) {
        log.debug("Obteniendo ingredientes activos del proveedor: {}", supplierId);
        supplierService.findById(supplierId);
        return ingredientsRepository.findActiveIngredientsBySupplier(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findActiveIngredientsByCategory(String category) {
        log.debug("Obteniendo ingredientes activos de categoría: {}", category);
        return ingredientsRepository.findActiveIngredientsByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> searchByName(String name) {
        log.debug("Buscando ingredientes por nombre: {}", name);
        return ingredientsRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> searchByCategory(String category) {
        log.debug("Buscando ingredientes por categoría: {}", category);
        return ingredientsRepository.findByCategoryContainingIgnoreCase(category);
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findLowStockIngredients() {
        log.debug("Obteniendo ingredientes con stock bajo");
        return ingredientsRepository.findLowStockIngredients();
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findExpiredIngredients() {
        log.debug("Obteniendo ingredientes vencidos");
        return ingredientsRepository.findExpiredIngredients();
    }

    @Transactional(readOnly = true)
    public List<Ingredients> findExpiringIngredientsNextWeek() {
        log.debug("Obteniendo ingredientes a vencer en próxima semana");
        return ingredientsRepository.findExpiringIngredientsNextWeek();
    }

    @Transactional
    public Ingredients updateQuantity(Integer id, BigDecimal newQuantity) {
        log.info("Actualizando cantidad de ingrediente {} a {}", id, newQuantity);

        Ingredients ingredients = findById(id);

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("La cantidad no puede ser negativa");
        }

        ingredients.setQuantity(newQuantity);
        Ingredients updatedIngredients = ingredientsRepository.save(ingredients);

        log.info("Cantidad de ingrediente {} actualizada", id);

        return updatedIngredients;
    }

    @Transactional
    public Ingredients adjustQuantity(Integer id, BigDecimal adjustment) {
        log.info("Ajustando cantidad de ingrediente {} por {}", id, adjustment);

        Ingredients ingredients = findById(id);
        BigDecimal newQuantity = ingredients.getQuantity().add(adjustment);

        if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El ajuste haría que la cantidad sea negativa");
        }

        ingredients.setQuantity(newQuantity);
        Ingredients updatedIngredients = ingredientsRepository.save(ingredients);

        log.info("Cantidad de ingrediente {} ajustada", id);

        return updatedIngredients;
    }

    @Transactional(readOnly = true)
    public List<String> getAllActiveCategories() {
        log.debug("Obteniendo todas las categorías activas");
        return ingredientsRepository.findAllActiveCategories();
    }

    @Transactional(readOnly = true)
    public List<String> getAllActiveUnits() {
        log.debug("Obteniendo todas las unidades activas");
        return ingredientsRepository.findAllActiveUnits();
    }

    @Transactional(readOnly = true)
    public long countActive() {
        log.debug("Contando ingredientes activos");
        return ingredientsRepository.countByStateTrue();
    }

    @Transactional(readOnly = true)
    public long countLowStock() {
        log.debug("Contando ingredientes con stock bajo");
        return ingredientsRepository.countLowStockIngredients();
    }

    @Transactional(readOnly = true)
    public long countExpired() {
        log.debug("Contando ingredientes vencidos");
        return ingredientsRepository.countExpiredIngredients();
    }

    @Transactional(readOnly = true)
    public long countExpiringNextWeek() {
        log.debug("Contando ingredientes a vencer próxima semana");
        return ingredientsRepository.countExpiringIngredientsNextWeek();
    }

    @Transactional(readOnly = true)
    public long countBySupplier(Integer supplierId) {
        log.debug("Contando ingredientes del proveedor: {}", supplierId);
        supplierService.findById(supplierId);
        return ingredientsRepository.countBySupplier_IdSupplier(supplierId);
    }
}