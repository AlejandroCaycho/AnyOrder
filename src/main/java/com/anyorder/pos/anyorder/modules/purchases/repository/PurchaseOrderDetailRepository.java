package com.anyorder.pos.anyorder.modules.purchases.repository;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {
    List<PurchaseOrderDetail> findByPurchaseOrder_IdPurchaseOrder(Integer idPurchaseOrder);
}
