package com.anyorder.pos.anyorder.modules.qr.service;

import com.anyorder.pos.anyorder.modules.qr.model.QrOrder;
import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import com.anyorder.pos.anyorder.modules.qr.repository.QrOrderRepository;
import com.anyorder.pos.anyorder.modules.qr.repository.QrSessionRepository;
import com.anyorder.pos.anyorder.modules.orders.model.Order;
import com.anyorder.pos.anyorder.modules.orders.model.OrderDetail;
import com.anyorder.pos.anyorder.modules.orders.service.OrderService;
import com.anyorder.pos.anyorder.modules.orders.repository.OrderRepository;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.anyorder.pos.anyorder.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrOrderService {

    private final QrOrderRepository qrOrderRepository;
    private final QrSessionRepository qrSessionRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<QrOrder> findBySessionId(Integer idSession) {
        return qrOrderRepository.findBySession_IdSession(idSession);
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findByToken(String token) {
        return qrOrderRepository.findBySession_SessionToken(token);
    }

    @Transactional
    public QrOrder create(QrOrder order) {
        log.info("Nuevo pedido QR en sesión: {}", order.getSession().getIdSession());
        
        QrSession session = qrSessionRepository.findById(order.getSession().getIdSession())
                .orElseThrow(() -> new RuntimeException("Sesión QR no encontrada"));
        
        if (session.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("La sesión QR no está activa");
        }

        order.setSession(session);
        order.setOrderStatus(QrOrder.QrOrderStatus.PENDIENTE);
        
        // Calculate subtotal if not provided or to ensure correctness
        if (order.getUnitPrice() == null || order.getUnitPrice().signum() == 0) {
            order.setUnitPrice(order.getPresentation().getPrice()); // Fallback or logic here
        }
        order.setSubtotal(order.getUnitPrice().multiply(new java.math.BigDecimal(order.getAmount())));

        return qrOrderRepository.save(order);
    }

    /**
     * Confirma un pedido QR y lo integra al flujo principal de pedidos.
     */
    @Transactional
    public QrOrder confirmOrder(Integer idQrOrder, Integer idUser) {
        log.info("Confirmando pedido QR #{}", idQrOrder);

        QrOrder qrOrder = qrOrderRepository.findById(idQrOrder)
                .orElseThrow(() -> new RuntimeException("Pedido QR no encontrado"));

        if (qrOrder.getOrderStatus() != QrOrder.QrOrderStatus.PENDIENTE) {
            throw new RuntimeException("El pedido QR ya fue procesado");
        }

        User user = userService.findById(idUser).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        QrSession session = qrOrder.getSession();

        // 1. Buscar si ya hay un pedido (Order) activo para esta mesa
        List<Order> tableOrders = orderRepository.findByTable_IdTable(session.getTable().getIdTable());
        Order activeOrder = tableOrders.stream()
                .filter(o -> o.getOrderStatus() != Order.OrderStatus.CERRADO && o.getOrderStatus() != Order.OrderStatus.CANCELADO)
                .findFirst()
                .orElse(null);

        // 2. Preparar el detalle del pedido
        OrderDetail detail = new OrderDetail();
        detail.setPresentation(qrOrder.getPresentation());
        detail.setAmount(qrOrder.getAmount());
        detail.setUnitPrice(qrOrder.getUnitPrice());
        detail.setSubtotal(qrOrder.getSubtotal());
        detail.setNotes("Generado desde QR: " + (qrOrder.getNotes() != null ? qrOrder.getNotes() : ""));

        List<OrderDetail> detailsList = new ArrayList<>();
        detailsList.add(detail);

        if (activeOrder != null) {
            // Agregar al pedido existente
            orderService.addDetails(activeOrder.getIdOrder(), detailsList);
        } else {
            // Crear nuevo pedido
            Order newOrder = new Order();
            newOrder.setTable(session.getTable());
            newOrder.setCustomer(session.getCustomer());
            newOrder.setUser(user);
            newOrder.setNumberOfPeople(session.getNumberOfPeople());
            newOrder.setTypeOfConsumption(Order.ConsumptionType.LOCAL);
            newOrder.setDetails(detailsList);
            
            orderService.create(newOrder);
        }

        // 3. Actualizar estado del pedido QR
        qrOrder.setOrderStatus(QrOrder.QrOrderStatus.CONFIRMADO);
        qrOrder.setConfirmedBy(user);
        qrOrder.setConfirmedAt(LocalDateTime.now());

        return qrOrderRepository.save(qrOrder);
    }

    @Transactional
    public void cancel(Integer id) {
        qrOrderRepository.findById(id).ifPresent(o -> {
            if (o.getOrderStatus() == QrOrder.QrOrderStatus.PENDIENTE) {
                o.setOrderStatus(QrOrder.QrOrderStatus.CANCELADO);
                qrOrderRepository.save(o);
            }
        });
    }
}
