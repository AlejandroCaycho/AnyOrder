package com.anyorder.pos.anyorder.modules.presentations_ingredients.rest;

import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredient;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.model.PresentationIngredientId;
import com.anyorder.pos.anyorder.modules.presentations_ingredients.service.PresentationIngredientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presentation-ingredients")
@CrossOrigin(origins = "*")
public class PresentationIngredientController {

    @Autowired
    private PresentationIngredientService service;

    @GetMapping("/presentation/{idPresentation}")
    public ResponseEntity<List<PresentationIngredient>> getByPresentation(@PathVariable Integer idPresentation) {
        return ResponseEntity.ok(service.findByPresentationId(idPresentation));
    }
    
    @GetMapping("/ingredient/{idIngredient}")
    public ResponseEntity<List<PresentationIngredient>> getByIngredient(@PathVariable Integer idIngredient) {
        return ResponseEntity.ok(service.findByIngredientId(idIngredient));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PresentationIngredient presentationIngredient) {
        try {
            PresentationIngredient created = service.save(presentationIngredient);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{idPresentation}/{idIngredient}")
    public ResponseEntity<?> delete(
            @PathVariable Integer idPresentation,
            @PathVariable Integer idIngredient) {
        try {
            PresentationIngredientId id = new PresentationIngredientId(idPresentation, idIngredient);
            service.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Relación eliminada exitosamente"));
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
