package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.OrderDetail;
import com.anyorder.pos.anyorder.service.OrderDetailService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
@Tag(name = "Detalles de Pedidos", description = "Gestión de líneas de pedidos con cálculo automático de precios y totales")
public class OrderDetailController {

    private final OrderDetailService detailService;

    @Operation(summary = "Listar todos los detalles")
    @GetMapping
    public ResponseEntity<List<OrderDetail>> getAll() {
        return ResponseEntity.ok(detailService.findAll());
    }

    @Operation(summary = "Obtener detalle por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetail> getById(@Parameter(description = "ID del detalle") @PathVariable Integer id) {
        return ResponseEntity.ok(detailService.findById(id));
    }

    @Operation(summary = "Detalles de un pedido", description = "Obtiene todos los items de un pedido")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetail>> getByOrderId(
            @Parameter(description = "ID del pedido") @PathVariable Integer orderId) {
        return ResponseEntity.ok(detailService.findByOrderId(orderId));
    }

    @Operation(summary = "Agregar presentación al pedido", description = "Agrega un item al pedido. El precio se calcula automáticamente según el tipo de consumo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Detalle agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Pedido o presentación no encontrada")
    })
    @PostMapping("/order/{orderId}")
    public ResponseEntity<OrderDetail> addToOrder(
            @Parameter(description = "ID del pedido") @PathVariable Integer orderId,
            @Valid @RequestBody OrderDetail detail) {
        OrderDetail created = detailService.addDetailToOrder(orderId, detail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar cantidad del item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle actualizado"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> update(
            @Parameter(description = "ID del detalle") @PathVariable Integer id,
            @Valid @RequestBody OrderDetail detail) {
        OrderDetail updated = detailService.updateDetail(id, detail);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar item del pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle eliminado y totales actualizados"),
            @ApiResponse(responseCode = "404", description = "Detalle no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> remove(
            @Parameter(description = "ID del detalle") @PathVariable Integer id) {
        detailService.removeDetailFromOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Detalle eliminado y totales del pedido actualizados");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Total de items del pedido")
    @GetMapping("/order/{orderId}/count-items")
    public ResponseEntity<Map<String, Integer>> getTotalItems(@PathVariable Integer orderId) {
        Map<String, Integer> response = new HashMap<>();
        response.put("totalItems", detailService.getTotalItems(orderId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Total en dinero del pedido")
    @GetMapping("/order/{orderId}/total-amount")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(@PathVariable Integer orderId) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalAmount", detailService.getTotalAmount(orderId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar detalles del pedido")
    @GetMapping("/order/{orderId}/count")
    public ResponseEntity<Map<String, Long>> countDetails(@PathVariable Integer orderId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", detailService.countDetailsByOrder(orderId));
        return ResponseEntity.ok(response);
    }
}