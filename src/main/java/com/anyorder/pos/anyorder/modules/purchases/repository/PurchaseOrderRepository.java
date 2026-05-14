package com.anyorder.pos.anyorder.modules.purchases.repository;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);
    List<PurchaseOrder> findBySupplier_IdSupplier(Integer idSupplier);
    List<PurchaseOrder> findByOrderStatus(PurchaseOrder.OrderStatus status);
}
