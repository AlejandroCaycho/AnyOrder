package com.anyorder.pos.anyorder.modules.purchases.service;

import com.anyorder.pos.anyorder.modules.inventory.model.InventoryMovement;
import com.anyorder.pos.anyorder.modules.inventory.service.InventoryService;
import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrder;
import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrderDetail;
import com.anyorder.pos.anyorder.modules.purchases.repository.PurchaseOrderRepository;
import com.anyorder.pos.anyorder.modules.purchases.repository.PurchaseOrderDetailRepository;
import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.ingredients.repository.IngredientRepository;
import com.anyorder.pos.anyorder.modules.users.repository.UserRepository;
import com.anyorder.pos.anyorder.modules.suppliers.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository detailRepository;
    private final IngredientRepository ingredientsRepository;
    private final SupplierService supplierService;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAllActive() {
        return purchaseOrderRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findById(Integer id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        log.info("Creando nueva orden de compra: {}", purchaseOrder.getOrderNumber());

        if (purchaseOrderRepository.existsByOrderNumber(purchaseOrder.getOrderNumber())) {
            throw new RuntimeException("Ya existe una orden de compra con el número: " + purchaseOrder.getOrderNumber());
        }

        validateBasicData(purchaseOrder);

        purchaseOrder.setTotal(BigDecimal.ZERO);
        purchaseOrder.setOrderStatus(PurchaseOrder.OrderStatus.PENDIENTE);
        purchaseOrder.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDIENTE);

        List<PurchaseOrderDetail> details = purchaseOrder.getDetails();
        purchaseOrder.setDetails(null); // Save header first without details to avoid transient errors if not cascaded properly

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (details != null) {
            for (PurchaseOrderDetail detail : details) {
                Ingredient ingredient = ingredientsRepository.findById(detail.getIngredient().getIdIngredient())
                        .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado: " + detail.getIngredient().getIdIngredient()));

                detail.setPurchaseOrder(saved);
                detail.setIngredient(ingredient);
                detail.setSubtotal(detail.getUnitPrice().multiply(detail.getQuantity()));
                detail.setReceivedQuantity(BigDecimal.ZERO);

                detailRepository.save(detail);
                totalAmount = totalAmount.add(detail.getSubtotal());
            }
        }

        saved.setTotal(totalAmount);
        saved.setDetails(details);
        return purchaseOrderRepository.save(saved);
    }

    @Transactional
    public PurchaseOrder updateStatus(Integer id, PurchaseOrder.OrderStatus status) {
        PurchaseOrder order = findById(id);
        
        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO || 
            order.getOrderStatus() == PurchaseOrder.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede cambiar el estado de una orden finalizada (RECIBIDO/CANCELADO)");
        }

        order.setOrderStatus(status);
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder markAsReceived(Integer id) {
        log.info("Marcando orden {} como recibida", id);

        PurchaseOrder order = findById(id);

        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO) {
            throw new RuntimeException("La orden ya está marcada como recibida");
        }
        
        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede recibir una orden cancelada");
        }

        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrder_IdPurchaseOrder(id);
        if (details.isEmpty()) {
            throw new RuntimeException("La orden no tiene detalles para recibir");
        }

        for (PurchaseOrderDetail detail : details) {
            // Register movement in inventory
            InventoryMovement movement = new InventoryMovement();
            movement.setIngredient(detail.getIngredient());
            movement.setMovementType(InventoryMovement.MovementType.ENTRADA);
            movement.setQuantity(detail.getQuantity());
            movement.setReason("Recepción de Orden de Compra #" + order.getOrderNumber());
            movement.setUser(order.getUser());
            movement.setUnitCost(detail.getUnitPrice());
            
            inventoryService.registerManualMovement(movement);

            detail.setReceivedQuantity(detail.getQuantity());
            detailRepository.save(detail);
        }

        order.setOrderStatus(PurchaseOrder.OrderStatus.RECIBIDO);
        order.setReceivedDate(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public void cancel(Integer id) {
        PurchaseOrder order = findById(id);
        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO) {
            throw new RuntimeException("No se puede cancelar una orden ya recibida");
        }
        order.setOrderStatus(PurchaseOrder.OrderStatus.CANCELADO);
        purchaseOrderRepository.save(order);
    }

    private void validateBasicData(PurchaseOrder po) {
        if (po.getUser() == null || po.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }
        if (po.getSupplier() == null || po.getSupplier().getIdSupplier() == null) {
            throw new RuntimeException("El proveedor es obligatorio");
        }
        if (po.getDeliveryDate() == null) {
            throw new RuntimeException("La fecha de entrega estimada es obligatoria");
        }
        
        userRepository.findById(po.getUser().getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        supplierService.findById(po.getSupplier().getIdSupplier())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }
}
