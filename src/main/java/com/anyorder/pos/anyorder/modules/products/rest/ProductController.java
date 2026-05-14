package com.anyorder.pos.anyorder.modules.products.rest;

import com.anyorder.pos.anyorder.modules.products.model.Product;
import com.anyorder.pos.anyorder.modules.products.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Product>> getActiveProducts() {
        return ResponseEntity.ok(productService.findAllActive());
    }

    @GetMapping("/active/details")
    public ResponseEntity<List<Product>> getActiveProductsWithDetails() {
        return ResponseEntity.ok(productService.findAllActiveWithDetails());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Integer id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Producto no encontrado")));
    }

    @GetMapping("/category/{idCategory}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(productService.findByCategory(idCategory));
    }

    @GetMapping("/category/{idCategory}/active")
    public ResponseEntity<List<Product>> getActiveProductsByCategory(@PathVariable Integer idCategory) {
        return ResponseEntity.ok(productService.findActiveByCategoryId(idCategory));
    }

    @GetMapping("/area/{idArea}")
    public ResponseEntity<List<Product>> getProductsByArea(@PathVariable Integer idArea) {
        return ResponseEntity.ok(productService.findByArea(idArea));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @PostMapping
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

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateProduct(@PathVariable Integer id) {
        try {
            productService.delete(id);
            return ResponseEntity.ok(createSuccessResponse("Producto desactivado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateProduct(@PathVariable Integer id) {
        try {
            Product product = productService.activate(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProductPermanently(@PathVariable Integer id) {
        try {
            productService.deletePermanently(id);
            return ResponseEntity.ok(createSuccessResponse("Producto eliminado permanentemente"));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("No se puede eliminar, el producto está asociado a presentaciones u otros registros."));
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
