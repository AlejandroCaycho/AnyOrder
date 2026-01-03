package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.QrOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QrOrderRepository extends JpaRepository<QrOrder, Integer> {

    List<QrOrder> findByOrderStatus(QrOrder.OrderStatus status);

    List<QrOrder> findBySession_IdSession(Integer sessionId);

    List<QrOrder> findByPresentation_IdPresentation(Integer presentationId);

    List<QrOrder> findByConfirmedBy_IdUser(Integer userId);

    @Query("SELECT o FROM QrOrder o WHERE o.session.idSession = :sessionId ORDER BY o.createdAt DESC")
    List<QrOrder> findOrdersBySessionOrderByDate(@Param("sessionId") Integer sessionId);

    @Query("SELECT o FROM QrOrder o WHERE o.session.idSession = :sessionId AND o.orderStatus = 'PENDIENTE' ORDER BY o.createdAt ASC")
    List<QrOrder> findPendingOrdersBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT o FROM QrOrder o WHERE o.orderStatus = 'PENDIENTE' ORDER BY o.createdAt ASC")
    List<QrOrder> findAllPendingOrders();

    @Query("SELECT o FROM QrOrder o WHERE o.session.table.idTable = :tableId AND o.orderStatus IN ('PENDIENTE', 'CONFIRMADO') ORDER BY o.createdAt DESC")
    List<QrOrder> findActiveOrdersByTable(@Param("tableId") Integer tableId);

    @Query("SELECT o FROM QrOrder o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<QrOrder> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM QrOrder o WHERE o.session.idSession = :sessionId")
    long countOrdersBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT COUNT(o) FROM QrOrder o WHERE o.session.idSession = :sessionId AND o.orderStatus = :status")
    long countOrdersBySessionAndStatus(
            @Param("sessionId") Integer sessionId,
            @Param("status") QrOrder.OrderStatus status);

    @Query("SELECT SUM(o.subtotal) FROM QrOrder o WHERE o.session.idSession = :sessionId AND o.orderStatus = 'CONFIRMADO'")
    BigDecimal calculateTotalBySession(@Param("sessionId") Integer sessionId);

    @Query("SELECT o FROM QrOrder o WHERE o.orderStatus = 'CONFIRMADO' AND o.confirmedAt >= :startDate AND o.confirmedAt <= :endDate")
    List<QrOrder> findConfirmedOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM QrOrder o WHERE o.orderStatus = :status")
    long countByStatus(@Param("status") QrOrder.OrderStatus status);
}