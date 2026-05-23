package com.anyorder.pos.anyorder.modules.sales.rest;

import com.anyorder.pos.anyorder.modules.sales.model.Sale;
import com.anyorder.pos.anyorder.modules.sales.model.SalePayment;
import com.anyorder.pos.anyorder.modules.sales.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * POST /api/sales/order        → cobrar un pedido específico
 * POST /api/sales/customer     → cobrar todos los pedidos de un cliente
 * PATCH /api/sales/{id}/annul  → anular una venta
 *
 * Body para cobrar:
 * {
 *   "sale": { "user": { "idUser": 1 }, "order": { "idOrder": 5 } },
 *   "payments": [
 *     { "paymentType": "EFECTIVO", "amount": 50.00, "cashReceived": 60.00 }
 *   ]
 * }
 */
@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    // ─── CONSULTAS ───────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Sale>> getAll() {
        return ResponseEntity.ok(saleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sale> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(saleService.findById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Sale>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(saleService.findByUser(userId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Sale>> getByCustomer(@PathVariable Integer customerId) {
        return ResponseEntity.ok(saleService.findByCustomer(customerId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<Sale>> getToday() {
        return ResponseEntity.ok(saleService.findToday());
    }

    @GetMapping("/today/total")
    public ResponseEntity<BigDecimal> getTodayTotal() {
        return ResponseEntity.ok(saleService.getTodayTotal());
    }

    @GetMapping("/range")
    public ResponseEntity<List<Sale>> getByRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return ResponseEntity.ok(saleService.findByDateRange(start, end));
    }

    @GetMapping("/top-presentations")
    public ResponseEntity<List<Object[]>> getTopPresentations() {
        return ResponseEntity.ok(saleService.getMostSoldPresentations());
    }

    // ─── COBROS ──────────────────────────────────────────────

    /**
     * Cobra un pedido específico (el ID de la orden va en sale.order.idOrder)
     */
    @PostMapping("/order")
    public ResponseEntity<Sale> createFromOrder(@RequestBody SaleRequest request) {
        return ResponseEntity.status(201)
                .body(saleService.createFromOrder(request.sale(), request.payments()));
    }

    /**
     * Cobra todos los pedidos ENTREGADOS de un cliente
     */
    @PostMapping("/customer")
    public ResponseEntity<Sale> createFromCustomer(@RequestBody SaleRequest request) {
        return ResponseEntity.status(201)
                .body(saleService.createFromCustomerOrders(request.sale(), request.payments()));
    }

    // ─── ESTADO ──────────────────────────────────────────────

    @PatchMapping("/{id}/annul")
    public ResponseEntity<Sale> annul(@PathVariable Integer id) {
        return ResponseEntity.ok(saleService.annul(id));
    }

    // ─── REQUEST RECORD ──────────────────────────────────────

    public record SaleRequest(Sale sale, List<SalePayment> payments) {}
}
