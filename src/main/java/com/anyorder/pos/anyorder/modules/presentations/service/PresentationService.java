package com.anyorder.pos.anyorder.modules.presentations.service;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.presentations.repository.PresentationRepository;
import com.anyorder.pos.anyorder.modules.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PresentationService {

    @Autowired
    private PresentationRepository presentationRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Presentation> findAll() {
        return presentationRepository.findAll();
    }

    public List<Presentation> findAllActive() {
        return presentationRepository.findByStateTrue();
    }

    public Optional<Presentation> findById(Integer id) {
        return presentationRepository.findById(id);
    }

    public List<Presentation> findByProduct(Integer idProduct) {
        return presentationRepository.findByIdProduct(idProduct);
    }

    public List<Presentation> findActiveByProductId(Integer idProduct) {
        return presentationRepository.findByIdProductAndStateTrue(idProduct);
    }

    @Transactional
    public Presentation create(Presentation presentation) {
        if (!productRepository.existsById(presentation.getIdProduct())) {
            throw new IllegalArgumentException("El producto con ID " + presentation.getIdProduct() + " no existe");
        }
        if (presentation.getState() == null) presentation.setState(true);
        return presentationRepository.save(presentation);
    }

    @Transactional
    public Presentation update(Integer id, Presentation presentationDetails) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        if (!productRepository.existsById(presentationDetails.getIdProduct())) {
            throw new IllegalArgumentException("El producto con ID " + presentationDetails.getIdProduct() + " no existe");
        }

        presentation.setIdProduct(presentationDetails.getIdProduct());
        presentation.setName(presentationDetails.getName());
        presentation.setDescription(presentationDetails.getDescription());
        presentation.setCost(presentationDetails.getCost());
        presentation.setPrice(presentationDetails.getPrice());
        presentation.setDeliveryPrice(presentationDetails.getDeliveryPrice());
        presentation.setTakeoutPrice(presentationDetails.getTakeoutPrice());
        presentation.setPromoPrice(presentationDetails.getPromoPrice());
        presentation.setPreparationTime(presentationDetails.getPreparationTime());
        presentation.setDishPhotoUrl(presentationDetails.getDishPhotoUrl());
        presentation.setState(presentationDetails.getState());

        return presentationRepository.save(presentation);
    }

    @Transactional
    public void delete(Integer id) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));
        presentation.setState(false);
        presentationRepository.save(presentation);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!presentationRepository.existsById(id)) {
            throw new RuntimeException("Presentación no encontrada con ID: " + id);
        }
        presentationRepository.deleteById(id);
    }

    @Transactional
    public Presentation activate(Integer id) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));
        presentation.setState(true);
        return presentationRepository.save(presentation);
    }
}
