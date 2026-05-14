package com.anyorder.pos.anyorder.modules.orders.rest;

import com.anyorder.pos.anyorder.modules.orders.model.Order;
import com.anyorder.pos.anyorder.modules.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(orderService.findByUser(userId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getByCustomer(@PathVariable Integer customerId) {
        return ResponseEntity.ok(orderService.findByCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody Order order) {
        return ResponseEntity.status(201).body(orderService.create(order));
    }

    /** Confirma el pedido: descuenta stock e inicia preparación */
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Order> confirm(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.confirm(id));
    }

    /** Marca el pedido como listo para entregar */
    @PatchMapping("/{id}/ready")
    public ResponseEntity<Order> markReady(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.markAsReady(id));
    }

    /** Marca el pedido como entregado (habilitado para cobrarse) */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<Order> deliver(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.deliver(id));
    }

    /** Cancela el pedido y devuelve stock si ya estaba confirmado */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Integer id) {
        orderService.cancel(id);
        return ResponseEntity.ok().build();
    }
}
