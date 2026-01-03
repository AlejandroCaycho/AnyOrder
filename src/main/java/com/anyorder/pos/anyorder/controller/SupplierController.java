package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Supplier;
import com.anyorder.pos.anyorder.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión de proveedores")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(summary = "Listar todos los proveedores")
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }

    @GetMapping("/active")
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<Supplier>> getAllActive() {
        return ResponseEntity.ok(supplierService.findAllActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<Supplier> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @GetMapping("/document/{documentNumber}")
    @Operation(summary = "Obtener proveedor por documento")
    public ResponseEntity<Supplier> getByDocumentNumber(@PathVariable String documentNumber) {
        return ResponseEntity.ok(supplierService.findByDocumentNumber(documentNumber));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo proveedor")
    public ResponseEntity<Supplier> create(@Valid @RequestBody Supplier supplier) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(supplierService.create(supplier));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<Supplier> update(
            @PathVariable Integer id,
            @Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(supplierService.update(id, supplier));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desactivar proveedor")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        supplierService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Proveedor desactivado correctamente");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activar proveedor")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        supplierService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Proveedor activado correctamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        supplierService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Proveedor eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar proveedores por nombre")
    public ResponseEntity<List<Supplier>> search(@RequestParam String name) {
        return ResponseEntity.ok(supplierService.searchByName(name));
    }

    @GetMapping("/count/active")
    @Operation(summary = "Contar proveedores activos")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", supplierService.countActive());
        return ResponseEntity.ok(response);
    }
}
