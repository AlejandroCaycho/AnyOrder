package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Category;
import com.anyorder.pos.anyorder.repository.CategoryRepository;
import com.anyorder.pos.anyorder.repository.ProductRepository;
import com.anyorder.pos.anyorder.repository.PresentationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PresentationRepository presentationRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllActive() {
        return categoryRepository.findByStateTrue();
    }

    public List<Category> findAllActiveOrdered() {
        return categoryRepository.findAllActiveOrderedByDisplayOrder();
    }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public List<Category> findBySection(String section) {
        return categoryRepository.findBySectionOrderedByDisplayOrder(section);
    }

    public List<Category> findDeliveryCategories() {
        return categoryRepository.findByDeliveryTrue();
    }

    @Transactional
    public Category create(Category category) {

        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        if (category.getDelivery() == null) {
            category.setDelivery(false);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }
        if (category.getState() == null) {
            category.setState(true);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        if (!category.getName().equals(categoryDetails.getName()) &&
                categoryRepository.existsByName(categoryDetails.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setSection(categoryDetails.getSection());
        category.setDelivery(categoryDetails.getDelivery());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setState(categoryDetails.getState());

        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        List<Integer> productIds = productRepository.findIdsByCategory(id);
        for (Integer productId : productIds) {
            deactivateProductAndPresentations(productId);
        }

        category.setState(false);
        categoryRepository.save(category);
    }

    @Transactional
    public void hardDelete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public Category activate(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        category.setState(true);
        categoryRepository.save(category);

        List<Integer> productIds = productRepository.findIdsByCategory(id);
        for (Integer productId : productIds) {
            activateProductAndPresentations(productId);
        }

        return category;
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    private void deactivateProductAndPresentations(Integer productId) {

        List<Integer> presentationIds = presentationRepository.findIdsByProduct(productId);
        for (Integer presentationId : presentationIds) {
            deactivatePresentation(presentationId);
        }

        productRepository.findById(productId).ifPresent(product -> {
            product.setState(false);
            productRepository.save(product);
        });
    }

    private void activateProductAndPresentations(Integer productId) {

        productRepository.findById(productId).ifPresent(product -> {
            product.setState(true);
            productRepository.save(product);
        });

        List<Integer> presentationIds = presentationRepository.findIdsByProduct(productId);
        for (Integer presentationId : presentationIds) {
            activatePresentation(presentationId);
        }
    }

    private void deactivatePresentation(Integer presentationId) {
        presentationRepository.findById(presentationId).ifPresent(presentation -> {
            presentation.setState(false);
            presentationRepository.save(presentation);
        });
    }

    private void activatePresentation(Integer presentationId) {
        presentationRepository.findById(presentationId).ifPresent(presentation -> {
            presentation.setState(true);
            presentationRepository.save(presentation);
        });
    }
}