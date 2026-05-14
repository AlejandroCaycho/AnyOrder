package com.anyorder.pos.anyorder.modules.orders.repository;

import com.anyorder.pos.anyorder.modules.orders.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder_IdOrder(Integer idOrder);
}
