package com.anyorder.pos.anyorder.modules.purchases.service;

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

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findById(Integer id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrder purchaseOrder, List<PurchaseOrderDetail> details) {
        log.info("Creando nueva orden de compra");

        if (purchaseOrder.getUser() == null || purchaseOrder.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }

        if (purchaseOrder.getSupplier() == null || purchaseOrder.getSupplier().getIdSupplier() == null) {
            throw new RuntimeException("El proveedor es obligatorio");
        }

        userRepository.findById(purchaseOrder.getUser().getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        supplierService.findById(purchaseOrder.getSupplier().getIdSupplier())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        purchaseOrder.setTotal(BigDecimal.ZERO);
        purchaseOrder.setOrderStatus(PurchaseOrder.OrderStatus.PENDIENTE);
        purchaseOrder.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDIENTE);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail detail : details) {
            Ingredient ingredient = ingredientsRepository.findById(detail.getIngredient().getIdIngredient())
                    .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

            detail.setPurchaseOrder(saved);
            detail.setIngredient(ingredient);
            detail.setSubtotal(detail.getUnitPrice().multiply(detail.getQuantity()));
            detail.setReceivedQuantity(BigDecimal.ZERO);

            detailRepository.save(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());
        }

        saved.setTotal(totalAmount);
        return purchaseOrderRepository.save(saved);
    }

    @Transactional
    public PurchaseOrder markAsReceived(Integer id) {
        log.info("Marcando orden {} como recibida", id);

        PurchaseOrder order = findById(id);

        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO) {
            throw new RuntimeException("La orden ya está marcada como recibida");
        }

        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrder_IdPurchaseOrder(id);
        for (PurchaseOrderDetail detail : details) {
            Ingredient ingredient = detail.getIngredient();
            ingredient.setQuantity(ingredient.getQuantity().add(detail.getQuantity()));
            ingredientsRepository.save(ingredient);

            detail.setReceivedQuantity(detail.getQuantity());
            detailRepository.save(detail);
        }

        order.setOrderStatus(PurchaseOrder.OrderStatus.RECIBIDO);
        order.setReceivedDate(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }
}
