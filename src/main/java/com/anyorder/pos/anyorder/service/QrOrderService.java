package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.*;
import com.anyorder.pos.anyorder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrOrderService {

    private final QrOrderRepository qrOrderRepository;
    private final QrSessionService qrSessionService;
    private final PresentationService presentationService;
    private final PresentationIngredientService presentationIngredientService;
    private final IngredientsRepository ingredientsRepository;
    private final UsersService usersService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Transactional
    public QrOrder create(QrOrder qrOrder) {
        log.info("Creando pedido QR");

        if (qrOrder.getSession() == null || qrOrder.getSession().getIdSession() == null) {
            throw new RuntimeException("La sesion QR es obligatoria");
        }

        QrSession session = qrSessionService.findById(qrOrder.getSession().getIdSession());

        if (session.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("La sesion QR no esta activa");
        }

        qrOrder.setSession(session);

        if (qrOrder.getPresentation() == null ||
                qrOrder.getPresentation().getIdPresentation() == null) {
            throw new RuntimeException("La presentacion es obligatoria");
        }

        Presentation presentation = presentationService.findById(
                qrOrder.getPresentation().getIdPresentation())
                .orElseThrow(() -> new RuntimeException("Presentacion no encontrada"));

        if (!presentation.getState()) {
            throw new RuntimeException("La presentacion no esta disponible");
        }

        qrOrder.setPresentation(presentation);

        if (qrOrder.getAmount() == null || qrOrder.getAmount() < 1) {
            throw new RuntimeException("La cantidad debe ser al menos 1");
        }
        if (qrOrder.getAmount() > 100) {
            throw new RuntimeException("La cantidad no puede exceder 100");
        }

        validateStockForQrOrder(presentation, qrOrder.getAmount());

        qrOrder.setUnitPrice(presentation.getPrice());
        qrOrder.setSubtotal(qrOrder.getUnitPrice()
                .multiply(BigDecimal.valueOf(qrOrder.getAmount())));

        if (qrOrder.getOrderStatus() == null) {
            qrOrder.setOrderStatus(QrOrder.OrderStatus.PENDIENTE);
        }

        QrOrder savedOrder = qrOrderRepository.save(qrOrder);
        log.info("Pedido QR creado: ID {}, {} x {}",
                savedOrder.getIdQrOrder(),
                savedOrder.getAmount(),
                presentation.getName());

        return savedOrder;
    }

    private void validateStockForQrOrder(Presentation presentation, Integer cantidad) {
        List<PresentationIngredient> ingredients = presentationIngredientService
                .findByPresentationId(presentation.getIdPresentation());

        StringBuilder errores = new StringBuilder();

        for (PresentationIngredient pi : ingredients) {
            BigDecimal cantidadRequerida = pi.getQuantity()
                    .multiply(new BigDecimal(cantidad));

            Ingredients ingredient = pi.getIngredient();

            if (ingredient.getQuantity().compareTo(cantidadRequerida) < 0) {
                errores.append(String.format(
                        "%s: Insuficiente (Necesario: %.2f %s, Disponible: %.2f %s)\n",
                        ingredient.getName(),
                        cantidadRequerida,
                        ingredient.getUnit(),
                        ingredient.getQuantity(),
                        ingredient.getUnit()));
            }
        }

        if (errores.length() > 0) {
            throw new RuntimeException("STOCK INSUFICIENTE:\n" + errores.toString());
        }
    }

    @Transactional
    public Order convertirSessionAOrder(Integer sessionId, Integer userId) {
        log.info("Convirtiendo sesion QR {} a Order", sessionId);

        QrSession session = qrSessionService.findById(sessionId);

        if (session.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("La sesion no esta activa");
        }

        List<QrOrder> pendingOrders = qrOrderRepository.findPendingOrdersBySession(sessionId);

        if (pendingOrders.isEmpty()) {
            throw new RuntimeException("No hay pedidos pendientes en esta sesion");
        }

        Users user = usersService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Order mainOrder = new Order();
        mainOrder.setUser(user);
        mainOrder.setTable(session.getTable());
        mainOrder.setConsumptionType(Order.ConsumptionType.LOCAL);
        mainOrder.setNumberOfPeople(session.getNumberOfPeople());
        mainOrder.setCustomer(session.getCustomer());

        if (session.getCustomerName() != null) {
            mainOrder.setCustomerNotes("Cliente: " + session.getCustomerName());
        }

        List<OrderDetail> details = new ArrayList<>();

        for (QrOrder qrOrder : pendingOrders) {
            OrderDetail detail = new OrderDetail();
            detail.setPresentation(qrOrder.getPresentation());
            detail.setAmount(qrOrder.getAmount());
            detail.setNotes(qrOrder.getNotes());
            details.add(detail);
        }

        mainOrder.setDetails(details);

        Order createdOrder = orderService.create(mainOrder);

        for (QrOrder qrOrder : pendingOrders) {
            qrOrder.setOrderStatus(QrOrder.OrderStatus.CONFIRMADO);
            qrOrder.setConfirmedBy(user);
            qrOrder.setConfirmedAt(LocalDateTime.now());
            qrOrderRepository.save(qrOrder);
        }

        log.info("Sesion QR {} convertida a Order {}", sessionId, createdOrder.getIdOrder());
        return createdOrder;
    }

    @Transactional
    public QrOrder confirmOrder(Integer id, Integer userId) {
        log.info("Confirmando pedido QR individual: {}", id);

        QrOrder qrOrder = findById(id);

        if (qrOrder.getOrderStatus() != QrOrder.OrderStatus.PENDIENTE) {
            throw new RuntimeException("Solo se pueden confirmar pedidos pendientes");
        }

        Users user = usersService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validateStockForQrOrder(qrOrder.getPresentation(), qrOrder.getAmount());

        qrOrder.setOrderStatus(QrOrder.OrderStatus.CONFIRMADO);
        qrOrder.setConfirmedBy(user);
        qrOrder.setConfirmedAt(LocalDateTime.now());

        QrOrder confirmed = qrOrderRepository.save(qrOrder);
        log.info("Pedido QR {} confirmado", id);

        return confirmed;
    }

    @Transactional
    public QrOrder cancelOrder(Integer id, String reason) {
        log.info("Cancelando pedido QR: {}", id);

        QrOrder qrOrder = findById(id);

        if (qrOrder.getOrderStatus() == QrOrder.OrderStatus.CONFIRMADO) {
            throw new RuntimeException(
                    "No se pueden cancelar pedidos ya confirmados. " +
                            "Debe cancelar el Order principal.");
        }

        if (qrOrder.getOrderStatus() == QrOrder.OrderStatus.CANCELADO) {
            throw new RuntimeException("El pedido ya esta cancelado");
        }

        qrOrder.setOrderStatus(QrOrder.OrderStatus.CANCELADO);

        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = qrOrder.getNotes() != null ? qrOrder.getNotes() : "";
            qrOrder.setNotes(currentNotes + "\nCancelacion: " + reason);
        }

        QrOrder cancelled = qrOrderRepository.save(qrOrder);
        log.info("Pedido QR {} cancelado", id);

        return cancelled;
    }

    @Transactional
    public QrOrder update(Integer id, QrOrder qrOrderData) {
        log.info("Actualizando pedido QR: {}", id);

        QrOrder qrOrder = findById(id);

        if (qrOrder.getOrderStatus() != QrOrder.OrderStatus.PENDIENTE) {
            throw new RuntimeException("Solo se pueden actualizar pedidos pendientes");
        }

        if (qrOrderData.getAmount() != null) {
            if (qrOrderData.getAmount() < 1 || qrOrderData.getAmount() > 100) {
                throw new RuntimeException("La cantidad debe estar entre 1 y 100");
            }

            validateStockForQrOrder(qrOrder.getPresentation(), qrOrderData.getAmount());

            qrOrder.setAmount(qrOrderData.getAmount());
            qrOrder.setSubtotal(qrOrder.getUnitPrice()
                    .multiply(BigDecimal.valueOf(qrOrder.getAmount())));
        }

        if (qrOrderData.getNotes() != null) {
            qrOrder.setNotes(qrOrderData.getNotes());
        }

        QrOrder updated = qrOrderRepository.save(qrOrder);
        log.info("Pedido QR {} actualizado", id);

        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando pedido QR: {}", id);

        QrOrder order = findById(id);

        if (order.getOrderStatus() != QrOrder.OrderStatus.PENDIENTE) {
            throw new RuntimeException("Solo se pueden eliminar pedidos pendientes");
        }

        qrOrderRepository.deleteById(id);
        log.info("Pedido QR {} eliminado", id);
    }

    @Transactional(readOnly = true)
    public QrOrder findById(Integer id) {
        return qrOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido QR no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findAll() {
        return qrOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findByStatus(QrOrder.OrderStatus status) {
        return qrOrderRepository.findByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findBySession(Integer sessionId) {
        qrSessionService.findById(sessionId);
        return qrOrderRepository.findOrdersBySessionOrderByDate(sessionId);
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findPendingOrdersBySession(Integer sessionId) {
        qrSessionService.findById(sessionId);
        return qrOrderRepository.findPendingOrdersBySession(sessionId);
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findAllPendingOrders() {
        return qrOrderRepository.findAllPendingOrders();
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findActiveOrdersByTable(Integer tableId) {
        return qrOrderRepository.findActiveOrdersByTable(tableId);
    }

    @Transactional(readOnly = true)
    public List<QrOrder> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return qrOrderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalBySession(Integer sessionId) {
        BigDecimal total = qrOrderRepository.calculateTotalBySession(sessionId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countOrdersBySession(Integer sessionId) {
        return qrOrderRepository.countOrdersBySession(sessionId);
    }

    @Transactional(readOnly = true)
    public long countOrdersBySessionAndStatus(Integer sessionId, QrOrder.OrderStatus status) {
        return qrOrderRepository.countOrdersBySessionAndStatus(sessionId, status);
    }

    @Transactional(readOnly = true)
    public long countByStatus(QrOrder.OrderStatus status) {
        return qrOrderRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public QrSessionSummary getSessionSummary(Integer sessionId) {
        QrSession session = qrSessionService.findById(sessionId);

        long totalOrders = countOrdersBySession(sessionId);
        long pendingCount = qrOrderRepository.countOrdersBySessionAndStatus(
                sessionId, QrOrder.OrderStatus.PENDIENTE);
        long confirmedCount = qrOrderRepository.countOrdersBySessionAndStatus(
                sessionId, QrOrder.OrderStatus.CONFIRMADO);
        BigDecimal total = calculateTotalBySession(sessionId);

        return new QrSessionSummary(
                session,
                totalOrders,
                pendingCount,
                confirmedCount,
                total);
    }

    public static class QrSessionSummary {
        public final QrSession session;
        public final long totalOrders;
        public final long pendingCount;
        public final long confirmedCount;
        public final BigDecimal total;

        public QrSessionSummary(QrSession session, long totalOrders,
                long pendingCount, long confirmedCount, BigDecimal total) {
            this.session = session;
            this.totalOrders = totalOrders;
            this.pendingCount = pendingCount;
            this.confirmedCount = confirmedCount;
            this.total = total;
        }
    }
}