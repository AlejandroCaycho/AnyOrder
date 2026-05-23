package com.anyorder.pos.anyorder.modules.presentations_ingredients.service;

import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredient;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredientId;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.repository.PresentationIngredientRepository;
import com.anyorder.pos.anyorder.modules.presentations.repository.PresentationRepository;
import com.anyorder.pos.anyorder.modules.ingredients.repository.IngredientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PresentationIngredientService {

    @Autowired
    private PresentationIngredientRepository repository;
    
    @Autowired
    private PresentationRepository presentationRepository;
    
    @Autowired
    private IngredientRepository ingredientRepository;

    public List<PresentationIngredient> findByPresentationId(Integer idPresentation) {
        return repository.findById_IdPresentation(idPresentation);
    }
    
    public List<PresentationIngredient> findByIngredientId(Integer idIngredient) {
        return repository.findById_IdIngredient(idIngredient);
    }

    @Transactional
    public PresentationIngredient save(PresentationIngredient presentationIngredient) {
        if (presentationIngredient.getPresentation() == null || presentationIngredient.getPresentation().getIdPresentation() == null) {
            throw new IllegalArgumentException("La presentación y su ID son obligatorios.");
        }
        if (presentationIngredient.getIngredient() == null || presentationIngredient.getIngredient().getIdIngredient() == null) {
            throw new IllegalArgumentException("El ingrediente y su ID son obligatorios.");
        }

        // Sincronizar ID compuesto para que Hibernate pueda trabajar
        if (presentationIngredient.getId() == null) {
            presentationIngredient.setId(new PresentationIngredientId());
        }
        presentationIngredient.getId().setIdPresentation(presentationIngredient.getPresentation().getIdPresentation());
        presentationIngredient.getId().setIdIngredient(presentationIngredient.getIngredient().getIdIngredient());

        if (!presentationRepository.existsById(presentationIngredient.getId().getIdPresentation())) {
            throw new IllegalArgumentException("La presentación no existe.");
        }
        if (!ingredientRepository.existsById(presentationIngredient.getId().getIdIngredient())) {
            throw new IllegalArgumentException("El ingrediente no existe.");
        }
        return repository.save(presentationIngredient);
    }

    @Transactional
    public void delete(PresentationIngredientId id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Relación no encontrada.");
        }
        repository.deleteById(id);
    }
}
