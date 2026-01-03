package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<PurchaseOrder> findBySupplier_IdSupplier(Integer supplierId);

    List<PurchaseOrder> findByUser_IdUser(Integer userId);

    List<PurchaseOrder> findByOrderStatus(PurchaseOrder.OrderStatus status);

    List<PurchaseOrder> findByPaymentStatus(PurchaseOrder.PaymentStatus status);

    List<PurchaseOrder> findByState(Boolean state);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.supplier.idSupplier = :supplierId AND po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findBySupplierAndDateRange(@Param("supplierId") Integer supplierId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderStatus = :status AND po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByStatusAndDateRange(@Param("status") PurchaseOrder.OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.orderStatus IN ('PENDIENTE', 'CONFIRMADO', 'EN_TRANSITO') ORDER BY po.deliveryDate ASC")
    List<PurchaseOrder> findPendingOrders();

    @Query("SELECT po FROM PurchaseOrder po WHERE po.deliveryDate < CURRENT_DATE AND po.orderStatus NOT IN ('RECIBIDO', 'CANCELADO') ORDER BY po.deliveryDate ASC")
    List<PurchaseOrder> findOverdueOrders();

    @Query("SELECT SUM(po.total) FROM PurchaseOrder po WHERE po.orderDate BETWEEN :startDate AND :endDate AND po.state = true")
    BigDecimal sumTotalByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(po.total) FROM PurchaseOrder po WHERE po.supplier.idSupplier = :supplierId AND po.state = true")
    BigDecimal sumTotalBySupplier(@Param("supplierId") Integer supplierId);

    long countByOrderStatus(PurchaseOrder.OrderStatus status);

    long countByPaymentStatus(PurchaseOrder.PaymentStatus status);

    long countBySupplier_IdSupplier(Integer supplierId);
}