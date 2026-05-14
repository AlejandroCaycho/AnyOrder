package com.anyorder.pos.anyorder.modules.purchases.rest;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrder;
import com.anyorder.pos.anyorder.modules.purchases.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.findAllActive());
    }

    @GetMapping("/all")
    public ResponseEntity<List<PurchaseOrder>> getAllIncludingInactive() {
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> create(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return ResponseEntity.status(201).body(purchaseOrderService.create(purchaseOrder));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseOrder> updateStatus(
            @PathVariable Integer id, 
            @RequestParam PurchaseOrder.OrderStatus status) {
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, status));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrder> receive(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.markAsReceived(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Integer id) {
        purchaseOrderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
