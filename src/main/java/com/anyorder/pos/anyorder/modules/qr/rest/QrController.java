package com.anyorder.pos.anyorder.modules.qr.rest;

import com.anyorder.pos.anyorder.modules.qr.model.QrOrder;
import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import com.anyorder.pos.anyorder.modules.qr.service.QrOrderService;
import com.anyorder.pos.anyorder.modules.qr.service.QrSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QrController {

    private final QrSessionService sessionService;
    private final QrOrderService orderService;

    @GetMapping("/sessions/active")
    public ResponseEntity<List<QrSession>> getActiveSessions() {
        return ResponseEntity.ok(sessionService.findAllActive());
    }

    @PostMapping("/session")
    public ResponseEntity<QrSession> startSession(
            @RequestParam Integer idTable, 
            @RequestParam String customerName,
            @RequestParam(required = false) Integer numberOfPeople,
            @RequestParam(required = false) Integer idCustomer) {
        return ResponseEntity.status(201).body(sessionService.startSession(idTable, customerName, numberOfPeople, idCustomer));
    }

    @GetMapping("/session/{token}")
    public ResponseEntity<QrSession> getSessionByToken(@PathVariable String token) {
        return sessionService.findByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/session/{id}/close")
    public ResponseEntity<Void> closeSession(@PathVariable Integer id) {
        sessionService.closeSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/order")
    public ResponseEntity<QrOrder> createOrder(@Valid @RequestBody QrOrder order) {
        return ResponseEntity.status(201).body(orderService.create(order));
    }

    @GetMapping("/orders/session/{idSession}")
    public ResponseEntity<List<QrOrder>> getOrdersBySession(@PathVariable Integer idSession) {
        return ResponseEntity.ok(orderService.findBySessionId(idSession));
    }

    @GetMapping("/orders/token/{token}")
    public ResponseEntity<List<QrOrder>> getOrdersByToken(@PathVariable String token) {
        return ResponseEntity.ok(orderService.findByToken(token));
    }

    @PatchMapping("/order/{id}/confirm")
    public ResponseEntity<QrOrder> confirmOrder(
            @PathVariable Integer id,
            @RequestParam Integer idUser) {
        return ResponseEntity.ok(orderService.confirmOrder(id, idUser));
    }

    @DeleteMapping("/order/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Integer id) {
        orderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
