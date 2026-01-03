package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Product;
import com.anyorder.pos.anyorder.service.ProductService;
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
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productService.findAllActive());
    }

    @GetMapping("/active/details")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getActiveProductsWithDetails() {
        return ResponseEntity.ok(productService.findAllActiveWithDetails());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado")));
    }

    @GetMapping("/name/{name}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getProductByName(@PathVariable String name) {
        return productService.findByName(name)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado")));
    }

    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @GetMapping("/category/{idCategory}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(productService.findByCategory(idCategory));
    }

    @GetMapping("/category/{idCategory}/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getActiveProductsByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(productService.findActiveByCategoryId(idCategory));
    }

    @GetMapping("/area/{idArea}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getProductsByArea(@PathVariable Integer idArea) {
        return ResponseEntity.ok(productService.findByArea(idArea));
    }

    @GetMapping("/area/{idArea}/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Product>> getActiveProductsByArea(@PathVariable Integer idArea) {
        return ResponseEntity.ok(productService.findActiveByAreaId(idArea));
    }

    @GetMapping("/category/{idCategory}/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Long>> countProductsByCategory(@PathVariable Integer idCategory) {
        long count = productService.countByCategory(idCategory);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/area/{idArea}/count")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Long>> countProductsByArea(@PathVariable Integer idArea) {
        long count = productService.countByArea(idArea);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            Product newProduct = productService.create(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(updatedProduct);
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
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Producto desactivado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/hard")
    @Transactional
    public ResponseEntity<?> hardDeleteProduct(@PathVariable Integer id) {
        try {
            productService.hardDelete(id);
            return ResponseEntity.ok(createSuccessResponse("Producto eliminado permanentemente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<?> activateProduct(@PathVariable Integer id) {
        try {
            Product product = productService.activate(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/exists")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Boolean>> existsByNameInCategory(
            @RequestParam Integer idCategory,
            @RequestParam String name) {
        boolean exists = productService.existsByNameInCategory(idCategory, name);
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