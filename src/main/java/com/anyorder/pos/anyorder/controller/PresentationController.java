package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Presentation;
import com.anyorder.pos.anyorder.service.PresentationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presentations")
@CrossOrigin(origins = "*")
public class PresentationController {

    @Autowired
    private PresentationService presentationService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getAllPresentations() {
        return ResponseEntity.ok(presentationService.findAll());
    }

    @GetMapping("/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getActivePresentations() {
        return ResponseEntity.ok(presentationService.findAllActive());
    }

    @GetMapping("/active/details")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getActivePresentationsWithDetails() {
        return ResponseEntity.ok(presentationService.findAllActiveWithDetails());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPresentationById(@PathVariable Integer id) {
        return presentationService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Presentación no encontrada")));
    }

    @GetMapping("/name/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPresentationByName(@PathVariable String name) {
        return presentationService.findByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Presentación no encontrada")));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> searchPresentations(@RequestParam String name) {
        return ResponseEntity.ok(presentationService.searchByName(name));
    }

    @GetMapping("/product/{idProduct}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsByProduct(@PathVariable Integer idProduct) {
        return ResponseEntity.ok(presentationService.findByProduct(idProduct));
    }

    @GetMapping("/product/{idProduct}/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getActivePresentationsByProduct(@PathVariable Integer idProduct) {
        return ResponseEntity.ok(presentationService.findActiveByProductId(idProduct));
    }

    @GetMapping("/category/{idCategory}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(presentationService.findByCategory(idCategory));
    }

    @GetMapping("/area/{idArea}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsByArea(@PathVariable Integer idArea) {
        return ResponseEntity.ok(presentationService.findByArea(idArea));
    }

    @GetMapping("/price-range")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        return ResponseEntity.ok(presentationService.findByPriceRange(minPrice, maxPrice));
    }

    @GetMapping("/promo")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsWithPromo() {
        return ResponseEntity.ok(presentationService.findWithPromoPrice());
    }

    @GetMapping("/delivery")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsForDelivery() {
        return ResponseEntity.ok(presentationService.findAvailableForDelivery());
    }

    @GetMapping("/takeout")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsForTakeout() {
        return ResponseEntity.ok(presentationService.findAvailableForTakeout());
    }

    @GetMapping("/preparation-time")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Presentation>> getPresentationsByMaxPreparationTime(
            @RequestParam Integer maxTime) {
        return ResponseEntity.ok(presentationService.findByMaxPreparationTime(maxTime));
    }

    @GetMapping("/product/{idProduct}/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Long>> countPresentationsByProduct(@PathVariable Integer idProduct) {
        long count = presentationService.countByProduct(idProduct);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createPresentation(@Valid @RequestBody Presentation presentation) {
        try {
            Presentation newPresentation = presentationService.create(presentation);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPresentation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updatePresentation(
            @PathVariable Integer id,
            @Valid @RequestBody Presentation presentation) {
        try {
            Presentation updatedPresentation = presentationService.update(id, presentation);
            return ResponseEntity.ok(updatedPresentation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/price")
    @Transactional
    public ResponseEntity<?> updatePrice(
            @PathVariable Integer id,
            @RequestParam BigDecimal price) {
        try {
            Presentation presentation = presentationService.updatePrice(id, price);
            return ResponseEntity.ok(presentation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/promo-price")
    @Transactional
    public ResponseEntity<?> updatePromoPrice(
            @PathVariable Integer id,
            @RequestParam(required = false) BigDecimal promoPrice) {
        try {
            Presentation presentation = presentationService.updatePromoPrice(id, promoPrice);
            return ResponseEntity.ok(presentation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/upload-image")
    @Transactional
    public ResponseEntity<?> uploadPresentationImage(
            @PathVariable Integer id,
            @RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("El archivo de imagen no puede estar vacío"));
            }

            String imageUrl = presentationService.uploadImage(id, file);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Imagen subida exitosamente");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al subir la imagen: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/delete-image")
    @Transactional
    public ResponseEntity<?> deletePresentationImage(@PathVariable Integer id) {
        try {
            presentationService.deleteImage(id);
            return ResponseEntity.ok(createSuccessResponse("Imagen eliminada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deletePresentation(@PathVariable Integer id) {
        try {
            presentationService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Presentación desactivada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/hard")
    @Transactional
    public ResponseEntity<?> hardDeletePresentation(@PathVariable Integer id) {
        try {
            presentationService.hardDelete(id);
            return ResponseEntity.ok(createSuccessResponse("Presentación eliminada permanentemente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<?> activatePresentation(@PathVariable Integer id) {
        try {
            Presentation presentation = presentationService.activate(id);
            return ResponseEntity.ok(presentation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/exists/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> existsByName(@PathVariable String name) {
        boolean exists = presentationService.existsByName(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}