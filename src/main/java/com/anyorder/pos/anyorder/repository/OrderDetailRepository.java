package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    List<OrderDetail> findByOrder_IdOrder(Integer orderId);

    @Query("SELECT od FROM OrderDetail od WHERE od.order.idOrder = :orderId ORDER BY od.idDetail ASC")
    List<OrderDetail> findDetailsByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT SUM(od.amount) FROM OrderDetail od WHERE od.order.idOrder = :orderId")
    Integer sumAmountByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT SUM(od.subtotal) FROM OrderDetail od WHERE od.order.idOrder = :orderId")
    java.math.BigDecimal sumSubtotalByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT COUNT(od) FROM OrderDetail od WHERE od.order.idOrder = :orderId")
    long countByOrderId(@Param("orderId") Integer orderId);

    @Query("DELETE FROM OrderDetail od WHERE od.order.idOrder = :orderId")
    void deleteByOrderId(@Param("orderId") Integer orderId);
}