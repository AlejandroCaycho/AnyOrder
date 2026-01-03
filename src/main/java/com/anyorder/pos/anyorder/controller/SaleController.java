package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Sale;
import com.anyorder.pos.anyorder.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión completa de ventas: cabecera + detalles + pagos en un solo POST")
public class SaleController {

    private final SaleService saleService;

    @Operation(summary = "Listar todas las ventas")
    @GetMapping
    public ResponseEntity<List<Sale>> getAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @Operation(summary = "Obtener venta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Sale> getById(
            @Parameter(description = "ID de la venta") @PathVariable Integer id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    @Operation(summary = "Ventas por usuario", description = "Lista las ventas realizadas por un usuario específico")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Sale>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(saleService.findByUserId(userId));
    }

    @Operation(summary = "Ventas por cliente")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Sale>> getByCustomer(@PathVariable Integer customerId) {
        return ResponseEntity.ok(saleService.findByCustomerId(customerId));
    }

    @Operation(summary = "Ventas por pedido")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Sale>> getByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(saleService.findByOrderId(orderId));
    }

    @Operation(summary = "Ventas por estado", description = "Estado: 0=Anulado, 1=Activo, 2=Completado")
    @GetMapping("/state/{state}")
    public ResponseEntity<List<Sale>> getByState(@PathVariable Integer state) {
        return ResponseEntity.ok(saleService.findByState(state));
    }

    @Operation(summary = "Ventas por rango de fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<Sale>> getByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.findByDateRange(startDate, endDate));
    }

    @Operation(summary = "Ventas por estado y rango de fechas")
    @GetMapping("/state/{state}/date-range")
    public ResponseEntity<List<Sale>> getByStateAndDateRange(
            @PathVariable Integer state,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.findByStateAndDateRange(state, startDate, endDate));
    }

    @Operation(summary = "Ventas del día")
    @GetMapping("/today")
    public ResponseEntity<List<Sale>> getTodaySales() {
        return ResponseEntity.ok(saleService.findTodaySales());
    }

    @Operation(summary = "Ventas recientes")
    @GetMapping("/recent")
    public ResponseEntity<List<Sale>> getRecentSales(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(saleService.findRecentSales(limit));
    }

    @Operation(summary = "Ventas por usuario y rango de fechas")
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Sale>> getByUserAndDateRange(
            @PathVariable Integer userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.findByUserAndDateRange(userId, startDate, endDate));
    }

    @Operation(summary = "CREAR VENTA COMPLETA", description = "Crea una venta con cabecera + detalles + pagos en un solo POST. "
            +
            "Cierra el pedido automáticamente y actualiza info del cliente si existe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o pedido ya tiene venta")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Sale sale) {
        try {
            Sale created = saleService.create(sale);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar venta", description = "Solo permite actualizar el estado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody Sale sale) {
        try {
            Sale updated = saleService.update(id, sale);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Anular venta", description = "Cambia el estado de la venta a 0 (ANULADO)")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSale(@PathVariable Integer id) {
        try {
            saleService.cancelSale(id, "Cancelada por el usuario");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Venta anulada correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Contar ventas por estado")
    @GetMapping("/count/state/{state}")
    public ResponseEntity<Map<String, Long>> countByState(@PathVariable Integer state) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", saleService.countByState(state));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Total de ventas por rango de fechas", description = "Suma el monto total vendido en un período")
    @GetMapping("/sum/date-range")
    public ResponseEntity<Map<String, BigDecimal>> sumTotalByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", saleService.sumTotalByDateRange(startDate, endDate));
        return ResponseEntity.ok(response);
    }
}