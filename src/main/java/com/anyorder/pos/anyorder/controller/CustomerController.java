package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Customer;
import com.anyorder.pos.anyorder.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes del restaurante")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Listar todos los clientes")
    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.findAll());
    }

    @Operation(summary = "Listar clientes activos")
    @GetMapping("/active")
    public ResponseEntity<List<Customer>> getAllActive() {
        return ResponseEntity.ok(customerService.findAllActive());
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(
            @Parameter(description = "ID del cliente") @PathVariable Integer id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @Operation(summary = "Obtener cliente por documento")
    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<Customer> getByDocumentNumber(
            @PathVariable String documentNumber) {
        return ResponseEntity.ok(customerService.findByDocumentNumber(documentNumber));
    }

    @Operation(summary = "Obtener cliente por email")
    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getByEmail(
            @PathVariable String email) {
        return ResponseEntity.ok(customerService.findByEmail(email));
    }

    @Operation(summary = "Crear nuevo cliente")
    @PostMapping
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.create(customer));
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{id}")
    public ResponseEntity<Customer> update(
            @PathVariable Integer id,
            @Valid @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.update(id, customer));
    }

    @Operation(summary = "Desactivar cliente")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, String>> deactivate(@PathVariable Integer id) {
        customerService.deactivate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cliente desactivado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activar cliente")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activate(@PathVariable Integer id) {
        customerService.activate(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cliente activado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar cliente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        customerService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cliente eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar clientes por nombre")
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> search(@RequestParam String name) {
        return ResponseEntity.ok(customerService.searchByName(name));
    }

    @Operation(summary = "Obtener clientes por tipo")
    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<Customer>> getByCustomerType(
            @PathVariable Customer.CustomerType customerType) {
        return ResponseEntity.ok(customerService.findByCustomerType(customerType));
    }

    @Operation(summary = "Obtener clientes por tipo de documento")
    @GetMapping("/document-type/{documentType}")
    public ResponseEntity<List<Customer>> getByDocumentType(
            @PathVariable Customer.DocumentType documentType) {
        return ResponseEntity.ok(customerService.findByDocumentType(documentType));
    }

    @Operation(summary = "Obtener clientes VIP")
    @GetMapping("/vip")
    public ResponseEntity<List<Customer>> getVIPCustomers(
            @RequestParam(defaultValue = "5") Integer minPurchases) {
        return ResponseEntity.ok(customerService.findVIPCustomers(minPurchases));
    }

    @Operation(summary = "Obtener clientes recientes")
    @GetMapping("/recent")
    public ResponseEntity<List<Customer>> getRecentCustomers(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(customerService.findRecentCustomers(limit));
    }

    @Operation(summary = "Contar clientes activos")
    @GetMapping("/count/active")
    public ResponseEntity<Map<String, Long>> countActive() {
        Map<String, Long> response = new HashMap<>();
        response.put("total", customerService.countActive());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar clientes por tipo")
    @GetMapping("/count/type/{customerType}")
    public ResponseEntity<Map<String, Long>> countByCustomerType(
            @PathVariable Customer.CustomerType customerType) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", customerService.countByCustomerType(customerType));
        return ResponseEntity.ok(response);
    }
}
