package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.QrOrder;
import com.anyorder.pos.anyorder.service.QrOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qr-orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos QR", description = "Gestión de pedidos realizados mediante QR")
public class QrOrderController {

    private final QrOrderService qrOrderService;

    @Operation(summary = "Listar todos los pedidos QR", description = "Obtiene todos los pedidos QR registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<QrOrder>> getAll() {
        return ResponseEntity.ok(qrOrderService.findAll());
    }

    @Operation(summary = "Obtener pedido QR por ID", description = "Busca un pedido QR específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QrOrder> getById(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(qrOrderService.findById(id));
    }

    @Operation(summary = "Crear nuevo pedido QR", description = "Registra un nuevo pedido QR en una sesión activa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o sesión no activa")
    })
    @PostMapping
    public ResponseEntity<QrOrder> create(@Valid @RequestBody QrOrder qrOrder) {
        QrOrder created = qrOrderService.create(qrOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar pedido QR", description = "Modifica los datos de un pedido QR pendiente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<QrOrder> update(
            @Parameter(description = "ID del pedido", required = true) @PathVariable Integer id,
            @Valid @RequestBody QrOrder qrOrder) {
        QrOrder updated = qrOrderService.update(id, qrOrder);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar pedido QR", description = "Elimina un pedido QR pendiente del sistema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        qrOrderService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Pedido QR eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmar pedido QR", description = "Confirma un pedido QR pendiente")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<QrOrder> confirmOrder(
            @PathVariable Integer id,
            @RequestParam Integer userId) {
        return ResponseEntity.ok(qrOrderService.confirmOrder(id, userId));
    }

    @Operation(summary = "Cancelar pedido QR", description = "Cancela un pedido QR pendiente")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<QrOrder> cancelOrder(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(qrOrderService.cancelOrder(id, reason));
    }

    @Operation(summary = "Obtener pedidos por estado", description = "Lista pedidos según su estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<QrOrder>> getByStatus(
            @PathVariable QrOrder.OrderStatus status) {
        return ResponseEntity.ok(qrOrderService.findByStatus(status));
    }

    @Operation(summary = "Obtener pedidos de una sesión", description = "Lista todos los pedidos de una sesión QR")
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<QrOrder>> getBySession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(qrOrderService.findBySession(sessionId));
    }

    @Operation(summary = "Obtener pedidos pendientes de una sesión", description = "Lista pedidos pendientes de una sesión QR")
    @GetMapping("/session/{sessionId}/pending")
    public ResponseEntity<List<QrOrder>> getPendingBySession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(qrOrderService.findPendingOrdersBySession(sessionId));
    }

    @Operation(summary = "Obtener todos los pedidos pendientes", description = "Lista todos los pedidos QR pendientes")
    @GetMapping("/pending")
    public ResponseEntity<List<QrOrder>> getAllPending() {
        return ResponseEntity.ok(qrOrderService.findAllPendingOrders());
    }

    @Operation(summary = "Obtener pedidos activos por mesa", description = "Lista pedidos activos de una mesa")
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<List<QrOrder>> getActiveByTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(qrOrderService.findActiveOrdersByTable(tableId));
    }

    @Operation(summary = "Obtener pedidos por rango de fechas", description = "Lista pedidos entre dos fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<QrOrder>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(qrOrderService.findOrdersBetweenDates(startDate, endDate));
    }

    @Operation(summary = "Contar pedidos por sesión", description = "Retorna el número total de pedidos de una sesión")
    @GetMapping("/count/session/{sessionId}")
    public ResponseEntity<Map<String, Long>> countBySession(@PathVariable Integer sessionId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", qrOrderService.countOrdersBySession(sessionId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar pedidos por sesión y estado", description = "Retorna el número de pedidos de una sesión según estado")
    @GetMapping("/count/session/{sessionId}/status/{status}")
    public ResponseEntity<Map<String, Long>> countBySessionAndStatus(
            @PathVariable Integer sessionId,
            @PathVariable QrOrder.OrderStatus status) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", qrOrderService.countOrdersBySessionAndStatus(sessionId, status));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular total de sesión", description = "Retorna el monto total de pedidos confirmados de una sesión")
    @GetMapping("/total/session/{sessionId}")
    public ResponseEntity<Map<String, BigDecimal>> calculateTotalBySession(@PathVariable Integer sessionId) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", qrOrderService.calculateTotalBySession(sessionId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar pedidos por estado", description = "Retorna el número de pedidos según estado")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Map<String, Long>> countByStatus(
            @PathVariable QrOrder.OrderStatus status) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", qrOrderService.countByStatus(status));
        return ResponseEntity.ok(response);
    }
}