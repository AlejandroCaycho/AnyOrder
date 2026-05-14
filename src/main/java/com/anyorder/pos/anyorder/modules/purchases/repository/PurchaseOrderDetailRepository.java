package com.anyorder.pos.anyorder.modules.purchases.repository;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {

    List<PurchaseOrderDetail> findByPurchaseOrder_IdPurchaseOrder(Integer idPurchaseOrder);

    List<PurchaseOrderDetail> findByIngredient_IdIngredient(Integer idIngredient);

    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM PurchaseOrderDetail d WHERE d.purchaseOrder.idPurchaseOrder = :orderId")
    BigDecimal sumSubtotalByOrder(Integer orderId);
}
