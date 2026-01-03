package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Category;
import com.anyorder.pos.anyorder.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.findAllActive());
    }

    @GetMapping("/ordered")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getOrderedCategories() {
        return ResponseEntity.ok(categoryService.findAllActiveOrdered());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        return categoryService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Categoría no encontrada")));
    }

    @GetMapping("/name/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCategoryByName(@PathVariable String name) {
        return categoryService.findByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Categoría no encontrada")));
    }

    @GetMapping("/section/{section}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getCategoriesBySection(@PathVariable String section) {
        return ResponseEntity.ok(categoryService.findBySection(section));
    }

    @GetMapping("/delivery")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Category>> getDeliveryCategories() {
        return ResponseEntity.ok(categoryService.findDeliveryCategories());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createCategory(@Valid @RequestBody Category category) {
        try {
            Category newCategory = categoryService.create(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody Category category) {
        try {
            Category updatedCategory = categoryService.update(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Categoría desactivada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/hard")
    @Transactional
    public ResponseEntity<?> hardDeleteCategory(@PathVariable Integer id) {
        try {
            categoryService.hardDelete(id);
            return ResponseEntity.ok(createSuccessResponse("Categoría eliminada permanentemente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<?> activateCategory(@PathVariable Integer id) {
        try {
            Category category = categoryService.activate(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/exists/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> existsByName(@PathVariable String name) {
        boolean exists = categoryService.existsByName(name);
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