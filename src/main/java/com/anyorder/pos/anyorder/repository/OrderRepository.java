package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByOrderStatus(Order.OrderStatus status);

    List<Order> findByConsumptionType(Order.ConsumptionType type);

    List<Order> findByUser_IdUser(Integer userId);

    List<Order> findByCustomer_IdCustomer(Integer customerId);

    List<Order> findByTable_IdTable(Integer tableId);

    List<Order> findByConfirmed(Boolean confirmed);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByStatusAndDateRange(
            @Param("status") Order.OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.consumptionType = :type AND o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findByConsumptionTypeAndDateRange(
            @Param("type") Order.ConsumptionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
    long countByOrderStatus(@Param("status") Order.OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.consumptionType = :type")
    long countByConsumptionType(@Param("type") Order.ConsumptionType type);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumTotalByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.table.idTable = :tableId AND o.orderStatus IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO')")
    List<Order> findActiveOrdersByTable(@Param("tableId") Integer tableId);
}