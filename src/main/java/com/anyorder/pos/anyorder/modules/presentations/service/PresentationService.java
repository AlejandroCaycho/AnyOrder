package com.anyorder.pos.anyorder.modules.presentations.service;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.presentations.repository.PresentationRepository;
import com.anyorder.pos.anyorder.modules.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        return presentationRepository.findByProduct_IdProduct(idProduct);
    }

    public List<Presentation> findActiveByProductId(Integer idProduct) {
        return presentationRepository.findByProduct_IdProductAndStateTrue(idProduct);
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

    @Transactional
    public Presentation uploadPhoto(Integer id, MultipartFile file) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada"));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "png";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            }

            String filename = "presentation_" + id + "_" + System.currentTimeMillis() + "." + extension;

            String projectPath = System.getProperty("user.dir");
            File baseDir = new File(projectPath);
            if (new File(baseDir, "AnyOrder-be").exists()) {
                baseDir = new File(baseDir, "AnyOrder-be");
            }
            File srcDir = new File(baseDir, "src/main/resources/static/presentaciones");
            File targetDir = new File(baseDir, "target/classes/static/presentaciones");

            if (!srcDir.exists()) srcDir.mkdirs();
            if (!targetDir.exists()) targetDir.mkdirs();

            if (presentation.getDishPhotoUrl() != null) {
                String oldPhotoPath = presentation.getDishPhotoUrl();
                if (oldPhotoPath.startsWith("/presentaciones/")) {
                    String oldFilename = oldPhotoPath.substring("/presentaciones/".length());
                    new File(srcDir, oldFilename).delete();
                    new File(targetDir, oldFilename).delete();
                }
            }

            File srcFile = new File(srcDir, filename);
            File targetFile = new File(targetDir, filename);

            Files.copy(file.getInputStream(), srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            try {
                Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // Ignore target copy failures if classes folder doesn't exist yet
            }

            presentation.setDishPhotoUrl("/presentaciones/" + filename);
            return presentationRepository.save(presentation);

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deletePhoto(Integer id) {
        Presentation presentation = presentationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presentación no encontrada"));

        if (presentation.getDishPhotoUrl() != null) {
            try {
                String photoPath = presentation.getDishPhotoUrl();
                if (photoPath.startsWith("/presentaciones/")) {
                    String filename = photoPath.substring("/presentaciones/".length());
                    String projectPath = System.getProperty("user.dir");
                    File baseDir = new File(projectPath);
                    if (new File(baseDir, "AnyOrder-be").exists()) {
                        baseDir = new File(baseDir, "AnyOrder-be");
                    }
                    new File(new File(baseDir, "src/main/resources/static/presentaciones"), filename).delete();
                    new File(new File(baseDir, "target/classes/static/presentaciones"), filename).delete();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error al eliminar la imagen: " + e.getMessage(), e);
            }
        }

        presentation.setDishPhotoUrl(null);
        presentationRepository.save(presentation);
    }
}
