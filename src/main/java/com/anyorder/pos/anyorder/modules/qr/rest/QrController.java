package com.anyorder.pos.anyorder.modules.qr.rest;

import com.anyorder.pos.anyorder.modules.qr.model.QrOrder;
import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import com.anyorder.pos.anyorder.modules.qr.service.QrOrderService;
import com.anyorder.pos.anyorder.modules.qr.service.QrSessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = "*")
public class QrController {

    @Autowired
    private QrSessionService sessionService;

    @Autowired
    private QrOrderService orderService;

    @PostMapping("/session")
    public ResponseEntity<QrSession> startSession(@RequestParam Integer idTable, @RequestParam String customerName) {
        return ResponseEntity.ok(sessionService.startSession(idTable, customerName));
    }

    @GetMapping("/session/{token}")
    public ResponseEntity<QrSession> getSessionByToken(@PathVariable String token) {
        return sessionService.findByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/order")
    public ResponseEntity<QrOrder> createOrder(@Valid @RequestBody QrOrder order) {
        return ResponseEntity.ok(orderService.create(order));
    }

    @GetMapping("/orders/session/{idSession}")
    public ResponseEntity<List<QrOrder>> getOrdersBySession(@PathVariable Integer idSession) {
        return ResponseEntity.ok(orderService.findBySessionId(idSession));
    }
}
