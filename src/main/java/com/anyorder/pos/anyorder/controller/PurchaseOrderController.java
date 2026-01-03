package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.PurchaseOrder;
import com.anyorder.pos.anyorder.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Orders", description = "Gestión de órdenes de compra a proveedores")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "Listar todas las órdenes")
    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    @Operation(summary = "Obtener orden por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    @Operation(summary = "Buscar por número de orden")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<PurchaseOrder> getByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(purchaseOrderService.findByOrderNumber(orderNumber));
    }

    @Operation(summary = "Órdenes por proveedor")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrder>> getBySupplier(@PathVariable Integer supplierId) {
        return ResponseEntity.ok(purchaseOrderService.findBySupplier(supplierId));
    }

    @Operation(summary = "Órdenes por usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PurchaseOrder>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(purchaseOrderService.findByUser(userId));
    }

    @Operation(summary = "Órdenes por estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrder>> getByStatus(@PathVariable PurchaseOrder.OrderStatus status) {
        return ResponseEntity.ok(purchaseOrderService.findByOrderStatus(status));
    }

    @Operation(summary = "Órdenes por estado de pago")
    @GetMapping("/payment-status/{status}")
    public ResponseEntity<List<PurchaseOrder>> getByPaymentStatus(@PathVariable PurchaseOrder.PaymentStatus status) {
        return ResponseEntity.ok(purchaseOrderService.findByPaymentStatus(status));
    }

    @Operation(summary = "Órdenes por rango de fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<PurchaseOrder>> getByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(purchaseOrderService.findByDateRange(startDate, endDate));
    }

    @Operation(summary = "Órdenes pendientes")
    @GetMapping("/pending")
    public ResponseEntity<List<PurchaseOrder>> getPending() {
        return ResponseEntity.ok(purchaseOrderService.findPendingOrders());
    }

    @Operation(summary = "Órdenes vencidas")
    @GetMapping("/overdue")
    public ResponseEntity<List<PurchaseOrder>> getOverdue() {
        return ResponseEntity.ok(purchaseOrderService.findOverdueOrders());
    }

    @Operation(summary = "Crear orden de compra", description = "Crea una orden con sus detalles")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        try {
            PurchaseOrder created = purchaseOrderService.create(purchaseOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar orden")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody PurchaseOrder order) {
        try {
            PurchaseOrder updated = purchaseOrderService.update(id, order);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Marcar como recibida", description = "Marca la orden como recibida y actualiza el inventario")
    @PatchMapping("/{id}/receive")
    public ResponseEntity<?> markAsReceived(@PathVariable Integer id) {
        try {
            PurchaseOrder received = purchaseOrderService.markAsReceived(id);
            return ResponseEntity.ok(received);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Cancelar orden")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        try {
            purchaseOrderService.cancelOrder(id);
            return ResponseEntity.ok(Map.of("message", "Orden cancelada exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Total de compras por rango")
    @GetMapping("/sum/date-range")
    public ResponseEntity<Map<String, Object>> sumTotalByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(Map.of("total", purchaseOrderService.sumTotalByDateRange(startDate, endDate)));
    }

    @Operation(summary = "Contar por estado")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Map<String, Long>> countByStatus(@PathVariable PurchaseOrder.OrderStatus status) {
        return ResponseEntity.ok(Map.of("total", purchaseOrderService.countByStatus(status)));
    }
}