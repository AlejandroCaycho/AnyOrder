package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Presentation;
import com.anyorder.pos.anyorder.repository.PresentationRepository;
import com.anyorder.pos.anyorder.repository.ProductRepository;
import com.anyorder.pos.anyorder.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class PresentationService {

    @Autowired
    private PresentationRepository presentationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Presentation> findAll() {
        return presentationRepository.findAll();
    }

    public List<Presentation> findAllActive() {
        return presentationRepository.findByStateTrue();
    }

    public List<Presentation> findAllActiveWithDetails() {
        return presentationRepository.findAllActiveWithDetails();
    }

    public Optional<Presentation> findById(Integer id) {
        return presentationRepository.findById(id);
    }

    public Optional<Presentation> findByName(String name) {
        return presentationRepository.findByName(name);
    }

    public List<Presentation> searchByName(String name) {
        return presentationRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Presentation> findByProduct(Integer idProduct) {
        return presentationRepository.findByProductWithDetails(idProduct);
    }

    public List<Presentation> findActiveByProductId(Integer idProduct) {
        return presentationRepository.findByIdProductAndStateTrue(idProduct);
    }

    public List<Presentation> findByCategory(Integer idCategory) {
        return presentationRepository.findByCategoryId(idCategory);
    }

    public List<Presentation> findByArea(Integer idArea) {
        return presentationRepository.findByAreaId(idArea);
    }

    public List<Presentation> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return presentationRepository.findByPriceRange(minPrice, maxPrice);
    }

    public List<Presentation> findWithPromoPrice() {
        return presentationRepository.findWithPromoPrice();
    }

    public List<Presentation> findByMaxPreparationTime(Integer maxTime) {
        return presentationRepository.findByMaxPreparationTime(maxTime);
    }

    public List<Presentation> findAvailableForDelivery() {
        return presentationRepository.findAvailableForDelivery();
    }

    public List<Presentation> findAvailableForTakeout() {
        return presentationRepository.findAvailableForTakeout();
    }

    public long countByProduct(Integer idProduct) {
        return presentationRepository.countByIdProductAndStateTrue(idProduct);
    }

    @Transactional
    public Presentation create(Presentation presentation) {

        if (!productRepository.existsById(presentation.getIdProduct())) {
            throw new IllegalArgumentException("El producto con ID " + presentation.getIdProduct() + " no existe");
        }

        if (presentationRepository.existsByName(presentation.getName())) {
            throw new IllegalArgumentException("Ya existe una presentación con ese nombre");
        }

        if (presentation.getPrice().compareTo(presentation.getCost()) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual al costo");
        }

        if (presentation.getCost() == null) {
            presentation.setCost(BigDecimal.ZERO);
        }
        if (presentation.getPreparationTime() == null) {
            presentation.setPreparationTime(0);
        }
        if (presentation.getState() == null) {
            presentation.setState(true);
        }

        return presentationRepository.save(presentation);
    }

    @Transactional
    public Presentation update(Integer id, Presentation presentationDetails) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        if (!productRepository.existsById(presentationDetails.getIdProduct())) {
            throw new IllegalArgumentException(
                    "El producto con ID " + presentationDetails.getIdProduct() + " no existe");
        }

        if (!presentation.getName().equals(presentationDetails.getName()) &&
                presentationRepository.existsByName(presentationDetails.getName())) {
            throw new IllegalArgumentException("Ya existe una presentación con ese nombre");
        }

        if (presentationDetails.getPrice().compareTo(presentationDetails.getCost()) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual al costo");
        }

        // Actualizar campos básicos
        presentation.setIdProduct(presentationDetails.getIdProduct());
        presentation.setName(presentationDetails.getName());
        presentation.setDescription(presentationDetails.getDescription());
        presentation.setCost(presentationDetails.getCost());
        presentation.setPrice(presentationDetails.getPrice());
        presentation.setDeliveryPrice(presentationDetails.getDeliveryPrice());
        presentation.setTakeoutPrice(presentationDetails.getTakeoutPrice());
        presentation.setPromoPrice(presentationDetails.getPromoPrice());
        presentation.setPreparationTime(presentationDetails.getPreparationTime());
        presentation.setState(presentationDetails.getState());

        // NO actualizar dishPhotoUrl aquí - tiene su propio endpoint

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
    public void hardDelete(Integer id) {
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
        presentationRepository.save(presentation);

        activateParentProduct(presentation.getIdProduct());

        return presentation;
    }

    @Transactional
    public Presentation updatePrice(Integer id, BigDecimal newPrice) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        if (newPrice.compareTo(presentation.getCost()) < 0) {
            throw new IllegalArgumentException("El precio debe ser mayor o igual al costo");
        }

        presentation.setPrice(newPrice);
        return presentationRepository.save(presentation);
    }

    @Transactional
    public Presentation updatePromoPrice(Integer id, BigDecimal promoPrice) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        if (promoPrice != null && promoPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio promocional debe ser mayor o igual a 0");
        }

        presentation.setPromoPrice(promoPrice);
        return presentationRepository.save(presentation);
    }

    @Transactional
    public String uploadImage(Integer id, MultipartFile file) throws IOException {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen válida");
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("El archivo no debe exceder 5MB");
        }

        String uploadDir = "src/main/resources/static/presentaciones";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "presentation_" + id + "_" + System.currentTimeMillis() + fileExtension;

        Path filePath = uploadPath.resolve(newFilename);
        Files.write(filePath, file.getBytes());

        String imageUrl = "/presentaciones/" + newFilename;
        presentation.setDishPhotoUrl(imageUrl);
        presentationRepository.save(presentation);

        return imageUrl;
    }

    @Transactional
    public void deleteImage(Integer id) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada con ID: " + id));

        if (presentation.getDishPhotoUrl() == null || presentation.getDishPhotoUrl().isEmpty()) {
            throw new RuntimeException("La presentación no tiene imagen asociada");
        }

        try {
            String filename = presentation.getDishPhotoUrl().replace("/presentaciones/", "");
            Path filePath = Paths.get("src/main/resources/static/presentaciones/" + filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            presentation.setDishPhotoUrl(null);
            presentationRepository.save(presentation);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar la imagen: " + e.getMessage());
        }
    }

    public boolean existsByName(String name) {
        return presentationRepository.existsByName(name);
    }

    private void activateParentProduct(Integer productId) {
        productRepository.findById(productId).ifPresent(product -> {
            if (!product.getState()) {
                product.setState(true);
                productRepository.save(product);

                activateParentCategory(product.getIdCategory());
            }
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