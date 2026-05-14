package com.anyorder.pos.anyorder.modules.purchases.repository;

import com.anyorder.pos.anyorder.modules.purchases.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<PurchaseOrder> findBySupplier_IdSupplier(Integer idSupplier);

    List<PurchaseOrder> findByOrderStatus(PurchaseOrder.OrderStatus status);

    List<PurchaseOrder> findByUser_IdUser(Integer userId);

    List<PurchaseOrder> findByStateTrue();

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :start AND :end ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.state = true ORDER BY po.createdAt DESC")
    List<PurchaseOrder> findAllActive();
}
