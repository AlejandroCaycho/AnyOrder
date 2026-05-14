package com.anyorder.pos.anyorder.modules.presentations.rest;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.presentations.service.PresentationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Presentation>> getAllPresentations() {
        return ResponseEntity.ok(presentationService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Presentation>> getActivePresentations() {
        return ResponseEntity.ok(presentationService.findAllActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPresentationById(@PathVariable Integer id) {
        return presentationService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Presentación no encontrada")));
    }

    @GetMapping("/product/{idProduct}")
    public ResponseEntity<List<Presentation>> getPresentationsByProduct(@PathVariable Integer idProduct) {
        return ResponseEntity.ok(presentationService.findByProduct(idProduct));
    }

    @GetMapping("/product/{idProduct}/active")
    public ResponseEntity<List<Presentation>> getActivePresentationsByProduct(@PathVariable Integer idProduct) {
        return ResponseEntity.ok(presentationService.findActiveByProductId(idProduct));
    }

    @PostMapping
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

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivatePresentation(@PathVariable Integer id) {
        try {
            presentationService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Presentación desactivada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePresentationPermanently(@PathVariable Integer id) {
        try {
            presentationService.deletePermanently(id);
            return ResponseEntity.ok(createSuccessResponse("Presentación eliminada permanentemente"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("No se puede eliminar, la presentación está asociada a otros registros."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activatePresentation(@PathVariable Integer id) {
        try {
            Presentation presentation = presentationService.activate(id);
            return ResponseEntity.ok(presentation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
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
