package com.anyorder.pos.anyorder.modules.purchases.rest;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrder;
import com.anyorder.pos.anyorder.modules.purchases.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@CrossOrigin(origins = "*")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrder>> getAll() {
        return ResponseEntity.ok(purchaseOrderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.findById(id));
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrder> receive(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseOrderService.markAsReceived(id));
    }
}
