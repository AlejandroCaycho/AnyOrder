package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.PresentationIngredient;
import com.anyorder.pos.anyorder.model.PresentationIngredientId;
import com.anyorder.pos.anyorder.model.Presentation;
import com.anyorder.pos.anyorder.model.Ingredients;
import com.anyorder.pos.anyorder.repository.PresentationIngredientRepository;
import com.anyorder.pos.anyorder.repository.PresentationRepository;
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
public class PresentationIngredientService {

    private final PresentationIngredientRepository presentationIngredientRepository;
    private final PresentationRepository presentationRepository;
    private final IngredientsRepository ingredientsRepository;

    @Transactional(readOnly = true)
    public List<PresentationIngredient> findAll() {
        log.debug("Obteniendo todas las relaciones presentación-ingrediente");
        return presentationIngredientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PresentationIngredient> findByPresentationId(Integer presentationId) {
        log.debug("Obteniendo ingredientes de la presentación: {}", presentationId);
        return presentationIngredientRepository.findByPresentationId(presentationId);
    }

    @Transactional(readOnly = true)
    public List<PresentationIngredient> findByIngredientId(Integer ingredientId) {
        log.debug("Obteniendo presentaciones que usan el ingrediente: {}", ingredientId);
        return presentationIngredientRepository.findByIngredientId(ingredientId);
    }

    /**
     * Agregar ingrediente a una presentación
     */
    @Transactional
    public PresentationIngredient addIngredientToPresentation(Integer presentationId, Integer ingredientId,
            BigDecimal quantity, String unit, String notes) {
        log.info("Agregando ingrediente {} a presentación {}", ingredientId, presentationId);

        // Validar que la presentación existe
        Presentation presentation = presentationRepository.findById(presentationId)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + presentationId));

        // Validar que el ingrediente existe
        Ingredients ingredient = ingredientsRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado con ID: " + ingredientId));

        // Validar que no exista ya la relación
        if (presentationIngredientRepository.existsByIdPresentationAndIdIngredient(presentationId, ingredientId)) {
            throw new RuntimeException("El ingrediente ya está asignado a esta presentación");
        }

        // Validar cantidad
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // Validar unidad
        if (unit == null || unit.trim().isEmpty()) {
            throw new RuntimeException("La unidad es obligatoria");
        }

        PresentationIngredient pi = new PresentationIngredient();
        pi.setIdPresentation(presentationId);
        pi.setIdIngredient(ingredientId);
        pi.setPresentation(presentation);
        pi.setIngredient(ingredient);
        pi.setQuantity(quantity);
        pi.setUnit(unit);
        pi.setNotes(notes);

        PresentationIngredient saved = presentationIngredientRepository.save(pi);
        log.info("Ingrediente agregado exitosamente");

        return saved;
    }

    /**
     * Actualizar cantidad de un ingrediente en una presentación
     */
    @Transactional
    public PresentationIngredient updateQuantity(Integer presentationId, Integer ingredientId,
            BigDecimal newQuantity, String unit) {
        log.info("Actualizando cantidad del ingrediente {} en presentación {}", ingredientId, presentationId);

        PresentationIngredientId id = new PresentationIngredientId(presentationId, ingredientId);
        PresentationIngredient pi = presentationIngredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));

        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        pi.setQuantity(newQuantity);
        if (unit != null && !unit.trim().isEmpty()) {
            pi.setUnit(unit);
        }

        PresentationIngredient updated = presentationIngredientRepository.save(pi);
        log.info("Cantidad actualizada exitosamente");

        return updated;
    }

    /**
     * Remover ingrediente de una presentación
     */
    @Transactional
    public void removeIngredientFromPresentation(Integer presentationId, Integer ingredientId) {
        log.info("Removiendo ingrediente {} de presentación {}", ingredientId, presentationId);

        if (!presentationIngredientRepository.existsByIdPresentationAndIdIngredient(presentationId, ingredientId)) {
            throw new RuntimeException("El ingrediente no está asignado a esta presentación");
        }

        presentationIngredientRepository.deleteByIdPresentationAndIdIngredient(presentationId, ingredientId);
        log.info("Ingrediente removido exitosamente");
    }

    /**
     * Remover todos los ingredientes de una presentación
     */
    @Transactional
    public void removeAllIngredientsFromPresentation(Integer presentationId) {
        log.info("Removiendo todos los ingredientes de presentación {}", presentationId);

        // Validar que existe
        presentationRepository.findById(presentationId)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + presentationId));

        presentationIngredientRepository.deleteByIdPresentation(presentationId);
        log.info("Todos los ingredientes removidos exitosamente");
    }

    /**
     * Calcular el costo total de ingredientes de una presentación
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalIngredientCost(Integer presentationId) {
        log.debug("Calculando costo total de ingredientes para presentación: {}", presentationId);

        BigDecimal total = presentationIngredientRepository.sumIngredientCostByPresentation(presentationId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Verificar si hay stock suficiente de todos los ingredientes
     */
    @Transactional(readOnly = true)
    public boolean hasEnoughStockForPresentation(Integer presentationId, Integer portions) {
        log.debug("Verificando stock para {} porciones de presentación {}", portions, presentationId);

        List<PresentationIngredient> ingredients = findByPresentationId(presentationId);

        for (PresentationIngredient pi : ingredients) {
            BigDecimal requiredQuantity = pi.getQuantity().multiply(new BigDecimal(portions));
            if (pi.getIngredient().getQuantity().compareTo(requiredQuantity) < 0) {
                log.warn("Stock insuficiente del ingrediente: {}", pi.getIngredient().getName());
                return false;
            }
        }

        return true;
    }

    @Transactional(readOnly = true)
    public long countByPresentation(Integer presentationId) {
        log.debug("Contando ingredientes de presentación: {}", presentationId);
        return presentationIngredientRepository.countByIdPresentation(presentationId);
    }
}