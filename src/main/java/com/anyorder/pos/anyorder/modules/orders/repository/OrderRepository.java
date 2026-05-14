package com.anyorder.pos.anyorder.modules.orders.repository;

import com.anyorder.pos.anyorder.modules.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_IdUser(Integer idUser);

    List<Order> findByTable_IdTable(Integer idTable);

    List<Order> findByCustomer_IdCustomer(Integer idCustomer);

    List<Order> findByOrderStatus(Order.OrderStatus orderStatus);

    /** Busca todos los pedidos de un cliente con un estado específico */
    List<Order> findByCustomer_IdCustomerAndOrderStatus(Integer customerId, Order.OrderStatus orderStatus);
}
