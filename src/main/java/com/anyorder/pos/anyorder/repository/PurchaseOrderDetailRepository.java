package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {

    @Query("SELECT pod FROM PurchaseOrderDetail pod WHERE pod.purchaseOrder.idPurchaseOrder = :purchaseOrderId")
    List<PurchaseOrderDetail> findByPurchaseOrderId(@Param("purchaseOrderId") Integer purchaseOrderId);

    List<PurchaseOrderDetail> findByIngredient_IdIngredient(Integer ingredientId);

    @Query("SELECT SUM(pod.subtotal) FROM PurchaseOrderDetail pod WHERE pod.purchaseOrder.idPurchaseOrder = :purchaseOrderId")
    BigDecimal sumSubtotalByPurchaseOrderId(@Param("purchaseOrderId") Integer purchaseOrderId);

    @Query("SELECT COUNT(pod) FROM PurchaseOrderDetail pod WHERE pod.purchaseOrder.idPurchaseOrder = :purchaseOrderId")
    long countByPurchaseOrderId(@Param("purchaseOrderId") Integer purchaseOrderId);
}