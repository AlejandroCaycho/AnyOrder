package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Order;
import com.anyorder.pos.anyorder.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            log.debug("Obteniendo todos los pedidos");
            List<Order> orders = orderService.findAll();
            log.info("Se encontraron {} pedidos", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al obtener todos los pedidos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener los pedidos: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                log.warn("Intento de buscar pedido con ID inválido: {}", id);
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID debe ser un número mayor a 0"));
            }

            log.debug("Buscando pedido con ID: {}", id);
            Order order = orderService.findById(id);
            log.info("Pedido {} encontrado - Estado: {}, Total: {}",
                    id, order.getOrderStatus(), order.getTotal());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.error("Pedido {} no encontrado: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al buscar pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener el pedido"));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable Order.OrderStatus status) {
        try {
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El estado no puede ser nulo"));
            }

            log.debug("Buscando pedidos con estado: {}", status);
            List<Order> orders = orderService.findByOrderStatus(status);
            log.info("Se encontraron {} pedidos con estado {}", orders.size(), status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos por estado {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos por estado"));
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<?> getByConsumptionType(@PathVariable Order.ConsumptionType type) {
        try {
            if (type == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El tipo de consumo no puede ser nulo"));
            }

            log.debug("Buscando pedidos tipo: {}", type);
            List<Order> orders = orderService.findByConsumptionType(type);
            log.info("Se encontraron {} pedidos tipo {}", orders.size(), type);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos por tipo {}: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos por tipo"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID de usuario debe ser mayor a 0"));
            }

            log.debug("Buscando pedidos del usuario: {}", userId);
            List<Order> orders = orderService.findByUserId(userId);
            log.info("Usuario {} tiene {} pedidos", userId, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos del usuario {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos del usuario"));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getByCustomer(@PathVariable Integer customerId) {
        try {
            if (customerId == null || customerId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID de cliente debe ser mayor a 0"));
            }

            log.debug("Buscando pedidos del cliente: {}", customerId);
            List<Order> orders = orderService.findByCustomerId(customerId);
            log.info("Cliente {} tiene {} pedidos", customerId, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos del cliente {}: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos del cliente"));
        }
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<?> getByTable(@PathVariable Integer tableId) {
        try {
            if (tableId == null || tableId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID de mesa debe ser mayor a 0"));
            }

            log.debug("Buscando pedidos de la mesa: {}", tableId);
            List<Order> orders = orderService.findByTableId(tableId);
            log.info("Mesa {} tiene {} pedidos", tableId, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos de la mesa {}: {}", tableId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos de la mesa"));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Las fechas de inicio y fin son obligatorias"));
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            log.debug("Buscando pedidos entre {} y {}", startDate, endDate);
            List<Order> orders = orderService.findByDateRange(startDate, endDate);
            log.info("Se encontraron {} pedidos en el rango especificado", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos por rango de fechas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos por rango de fechas"));
        }
    }

    @GetMapping("/status/{status}/date-range")
    public ResponseEntity<?> getByStatusAndDateRange(
            @PathVariable Order.OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El estado no puede ser nulo"));
            }

            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Las fechas son obligatorias"));
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            log.debug("Buscando pedidos con estado {} entre {} y {}", status, startDate, endDate);
            List<Order> orders = orderService.findByStatusAndDateRange(status, startDate, endDate);
            log.info("Se encontraron {} pedidos", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos"));
        }
    }

    @GetMapping("/type/{type}/date-range")
    public ResponseEntity<?> getByTypeAndDateRange(
            @PathVariable Order.ConsumptionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (type == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El tipo no puede ser nulo"));
            }

            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Las fechas son obligatorias"));
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            log.debug("Buscando pedidos tipo {} entre {} y {}", type, startDate, endDate);
            List<Order> orders = orderService.findByConsumptionTypeAndDateRange(type, startDate, endDate);
            log.info("Se encontraron {} pedidos", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos"));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        try {
            log.debug("Obteniendo pedidos pendientes");
            List<Order> orders = orderService.findPendingOrders();
            log.info("Hay {} pedidos pendientes", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al obtener pedidos pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos pendientes"));
        }
    }

    @GetMapping("/ready")
    public ResponseEntity<?> getReady() {
        try {
            log.debug("Obteniendo pedidos listos");
            List<Order> orders = orderService.findReadyOrders();
            log.info("Hay {} pedidos listos", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al obtener pedidos listos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos listos"));
        }
    }

    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<?> getActiveByTable(@PathVariable Integer tableId) {
        try {
            if (tableId == null || tableId <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID de mesa debe ser mayor a 0"));
            }

            log.debug("Buscando pedidos activos de la mesa: {}", tableId);
            List<Order> orders = orderService.findActiveOrdersByTable(tableId);
            log.info("Mesa {} tiene {} pedidos activos", tableId, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error al buscar pedidos activos de la mesa {}: {}", tableId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener pedidos activos de la mesa"));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Order order) {
        try {
            if (order == null) {
                log.warn("Intento de crear pedido nulo");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El pedido no puede ser nulo"));
            }

            if (order.getDetails() == null || order.getDetails().isEmpty()) {
                log.warn("Intento de crear pedido sin items");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El pedido debe tener al menos 1 item"));
            }

            if (order.getUser() == null || order.getUser().getIdUser() == null) {
                log.warn("Intento de crear pedido sin usuario");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El usuario es obligatorio"));
            }

            if (order.getConsumptionType() == null) {
                log.warn("Intento de crear pedido sin tipo de consumo");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El tipo de consumo es obligatorio"));
            }

            if (order.getNumberOfPeople() == null || order.getNumberOfPeople() <= 0) {
                log.warn("Intento de crear pedido con número de personas inválido: {}",
                        order.getNumberOfPeople());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El número de personas debe ser al menos 1"));
            }

            if (order.getConsumptionType() == Order.ConsumptionType.LOCAL) {
                if (order.getTable() == null || order.getTable().getIdTable() == null) {
                    log.warn("Intento de crear pedido LOCAL sin mesa");
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Para pedidos LOCAL, la mesa es obligatoria"));
                }
            }

            if (order.getConsumptionType() == Order.ConsumptionType.DELIVERY) {
                if (order.getDeliveryAddress() == null || order.getDeliveryAddress().trim().isEmpty()) {
                    log.warn("Intento de crear pedido DELIVERY sin dirección");
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Para pedidos DELIVERY, la dirección es obligatoria"));
                }
            }

            log.info("Creando pedido {} con {} items - Usuario: {}",
                    order.getConsumptionType(),
                    order.getDetails().size(),
                    order.getUser().getIdUser());

            Order created = orderService.create(order);

            log.info("Pedido creado exitosamente - ID: {}, Total: {}, Items: {}",
                    created.getIdOrder(),
                    created.getTotal(),
                    created.getTotalItems());

            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (RuntimeException e) {
            log.error("Error de validación al crear pedido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al crear pedido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno al crear el pedido"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Order order) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID debe ser mayor a 0"));
            }

            if (order == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Los datos del pedido no pueden ser nulos"));
            }

            log.info("Actualizando pedido ID: {}", id);
            Order updated = orderService.update(id, order);

            log.info("Pedido {} actualizado exitosamente", id);
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            log.error("Error al actualizar pedido {}: {}", id, e.getMessage());

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno al actualizar el pedido"));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Order.OrderStatus newStatus) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID debe ser mayor a 0"));
            }

            if (newStatus == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El nuevo estado no puede ser nulo"));
            }

            log.info("Cambiando estado del pedido {} a {}", id, newStatus);
            orderService.updateOrderStatus(id, newStatus);

            log.info("Estado del pedido {} actualizado a {}", id, newStatus);
            return ResponseEntity.ok(createSuccessResponse(
                    "Estado actualizado a: " + newStatus,
                    Map.of("orderId", id, "newStatus", newStatus.toString())));

        } catch (RuntimeException e) {
            log.error("Error al cambiar estado del pedido {}: {}", id, e.getMessage());

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al cambiar estado del pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno al cambiar el estado"));
        }
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID debe ser mayor a 0"));
            }

            log.info("Confirmando pedido {} - Se descontará stock", id);
            orderService.confirmOrder(id);

            log.info("Pedido {} confirmado exitosamente - Stock descontado", id);
            return ResponseEntity.ok(createSuccessResponse(
                    "Pedido confirmado y enviado a preparación",
                    Map.of("orderId", id, "status", "CONFIRMED")));

        } catch (RuntimeException e) {
            log.error("Error al confirmar pedido {}: {}", id, e.getMessage());

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al confirmar pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno al confirmar el pedido"));
        }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El ID debe ser mayor a 0"));
            }

            log.warn("Cancelando pedido {} - Se liberará stock si estaba confirmado", id);
            orderService.cancelOrder(id);

            log.info("Pedido {} cancelado exitosamente", id);
            return ResponseEntity.ok(createSuccessResponse(
                    "Pedido cancelado",
                    Map.of("orderId", id, "status", "CANCELLED")));

        } catch (RuntimeException e) {
            log.error("Error al cancelar pedido {}: {}", id, e.getMessage());

            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse(e.getMessage()));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al cancelar pedido {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno al cancelar el pedido"));
        }
    }

    @GetMapping("/count/status/{status}")
    public ResponseEntity<?> countByStatus(@PathVariable Order.OrderStatus status) {
        try {
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El estado no puede ser nulo"));
            }

            log.debug("Contando pedidos con estado: {}", status);
            long count = orderService.countByOrderStatus(status);
            log.info("Total de pedidos con estado {}: {}", status, count);

            Map<String, Object> response = new HashMap<>();
            response.put("status", status.toString());
            response.put("total", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al contar pedidos por estado {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al contar pedidos"));
        }
    }

    @GetMapping("/count/type/{type}")
    public ResponseEntity<?> countByType(@PathVariable Order.ConsumptionType type) {
        try {
            if (type == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("El tipo no puede ser nulo"));
            }

            log.debug("Contando pedidos tipo: {}", type);
            long count = orderService.countByConsumptionType(type);
            log.info("Total de pedidos tipo {}: {}", type, count);

            Map<String, Object> response = new HashMap<>();
            response.put("type", type.toString());
            response.put("total", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al contar pedidos por tipo {}: {}", type, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al contar pedidos"));
        }
    }

    @GetMapping("/sum/date-range")
    public ResponseEntity<?> sumTotalByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Las fechas son obligatorias"));
            }

            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("La fecha de inicio no puede ser posterior a la fecha fin"));
            }

            log.debug("Calculando total de ventas entre {} y {}", startDate, endDate);
            BigDecimal total = orderService.sumTotalByDateRange(startDate, endDate);
            log.info("Total de ventas en el período: {}", total);

            Map<String, Object> response = new HashMap<>();
            response.put("startDate", startDate.toString());
            response.put("endDate", endDate.toString());
            response.put("total", total);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al calcular total de ventas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al calcular el total"));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("success", false);
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message, Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("success", true);
        if (data != null && !data.isEmpty()) {
            response.put("data", data);
        }
        return response;
    }
}