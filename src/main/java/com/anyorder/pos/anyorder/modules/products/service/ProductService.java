package com.anyorder.pos.anyorder.modules.products.service;

import com.anyorder.pos.anyorder.modules.products.model.Product;
import com.anyorder.pos.anyorder.modules.products.repository.ProductRepository;
import com.anyorder.pos.anyorder.modules.categories.repository.CategoryRepository;
import com.anyorder.pos.anyorder.modules.areas.repository.AreaRepository;
import com.anyorder.pos.anyorder.modules.presentations.repository.PresentationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private PresentationRepository presentationRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public List<Product> findAllActive() {
        return productRepository.findByStateTrue();
    }

    public List<Product> findAllActiveWithDetails() {
        return productRepository.findAllActiveWithDetails();
    }

    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Product> findByCategory(Integer idCategory) {
        return productRepository.findByCategoryWithDetails(idCategory);
    }

    public List<Product> findActiveByCategoryId(Integer idCategory) {
        return productRepository.findByIdCategoryAndStateTrue(idCategory);
    }

    public List<Product> findByArea(Integer idArea) {
        return productRepository.findByAreaWithDetails(idArea);
    }

    public List<Product> findActiveByAreaId(Integer idArea) {
        return productRepository.findByIdAreaAndStateTrue(idArea);
    }

    public long countByCategory(Integer idCategory) {
        return productRepository.countByIdCategoryAndStateTrue(idCategory);
    }

    public long countByArea(Integer idArea) {
        return productRepository.countByIdArea(idArea);
    }

    @Transactional
    public Product create(Product product) {
        if (!categoryRepository.existsById(product.getIdCategory())) {
            throw new IllegalArgumentException("La categoría con ID " + product.getIdCategory() + " no existe");
        }
        if (!areaRepository.existsById(product.getIdArea())) {
            throw new IllegalArgumentException("El área con ID " + product.getIdArea() + " no existe");
        }
        if (productRepository.existsByIdCategoryAndName(product.getIdCategory(), product.getName())) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre en esta categoría");
        }
        if (product.getState() == null) product.setState(true);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Integer id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (!categoryRepository.existsById(productDetails.getIdCategory())) {
            throw new IllegalArgumentException("La categoría con ID " + productDetails.getIdCategory() + " no existe");
        }
        if (!areaRepository.existsById(productDetails.getIdArea())) {
            throw new IllegalArgumentException("El área con ID " + productDetails.getIdArea() + " no existe");
        }

        if (!product.getName().equals(productDetails.getName()) ||
                !product.getIdCategory().equals(productDetails.getIdCategory())) {
            if (productRepository.existsByIdCategoryAndNameAndIdProductNot(
                    productDetails.getIdCategory(),
                    productDetails.getName(),
                    id)) {
                throw new IllegalArgumentException("Ya existe un producto con ese nombre en esta categoría");
            }
        }

        product.setIdCategory(productDetails.getIdCategory());
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setIdArea(productDetails.getIdArea());
        product.setState(productDetails.getState());

        return productRepository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        List<Integer> presentationIds = presentationRepository.findIdsByProduct(id);
        for (Integer presentationId : presentationIds) {
            deactivatePresentation(presentationId);
        }

        product.setState(false);
        productRepository.save(product);
    }

    @Transactional
    public void deletePermanently(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional
    public Product activate(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        product.setState(true);
        productRepository.save(product);

        activateParentCategory(product.getIdCategory());

        List<Integer> presentationIds = presentationRepository.findIdsByProduct(id);
        for (Integer presentationId : presentationIds) {
            activatePresentation(presentationId);
        }

        return product;
    }

    public boolean existsByNameInCategory(Integer idCategory, String name) {
        return productRepository.existsByIdCategoryAndName(idCategory, name);
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

    private void activateParentCategory(Integer categoryId) {
        categoryRepository.findById(categoryId).ifPresent(category -> {
            if (!category.getState()) {
                category.setState(true);
                categoryRepository.save(category);
            }
        });
    }
}
