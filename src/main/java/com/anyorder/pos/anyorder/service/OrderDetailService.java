package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.OrderDetail;
import com.anyorder.pos.anyorder.model.Order;
import com.anyorder.pos.anyorder.model.Presentation;
import com.anyorder.pos.anyorder.repository.OrderDetailRepository;
import com.anyorder.pos.anyorder.repository.OrderRepository;
import com.anyorder.pos.anyorder.repository.PresentationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDetailService {

    private final OrderDetailRepository detailRepository;
    private final OrderRepository orderRepository;
    private final PresentationRepository presentationRepository;

    @Transactional(readOnly = true)
    public List<OrderDetail> findAll() {
        log.debug("Obteniendo todos los detalles de pedidos");
        return detailRepository.findAll();
    }

    @Transactional(readOnly = true)
    public OrderDetail findById(Integer id) {
        log.debug("Buscando detalle con ID: {}", id);
        return detailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderDetail> findByOrderId(Integer orderId) {
        log.debug("Obteniendo detalles del pedido: {}", orderId);
        // Validar que el pedido existe
        orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + orderId));
        return detailRepository.findDetailsByOrderId(orderId);
    }

    @Transactional
    public OrderDetail addDetailToOrder(Integer orderId, OrderDetail detail) {
        log.info("Agregando detalle al pedido: {}", orderId);

        // Obtener pedido
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + orderId));

        // Validar que el pedido no esté cerrado o cancelado
        if (order.getOrderStatus() == Order.OrderStatus.CERRADO
                || order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se pueden agregar detalles a un pedido cerrado o cancelado");
        }

        // Obtener presentación
        Presentation presentation = presentationRepository.findById(detail.getPresentation().getIdPresentation())
                .orElseThrow(() -> new RuntimeException(
                        "Presentación no encontrada con ID: " + detail.getPresentation().getIdPresentation()));

        // Validar cantidad
        if (detail.getAmount() == null || detail.getAmount() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // Obtener precio según tipo de consumo
        BigDecimal price = getPriceByConsumptionType(presentation, order.getConsumptionType());

        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("La presentación no tiene precio para este tipo de consumo");
        }

        // Asignar valores
        detail.setOrder(order);
        detail.setPresentation(presentation);
        detail.setUnitPrice(price);
        detail.calculateSubtotal();

        // Guardar detalle
        OrderDetail saved = detailRepository.save(detail);
        log.info("Detalle guardado con ID: {}", saved.getIdDetail());

        // Actualizar totales del pedido
        updateOrderTotals(orderId);

        return saved;
    }

    @Transactional
    public OrderDetail updateDetail(Integer id, OrderDetail detailData) {
        log.info("Actualizando detalle con ID: {}", id);

        OrderDetail detail = findById(id);
        Order order = detail.getOrder();

        // No permitir cambios si el pedido está cerrado o cancelado
        if (order.getOrderStatus() == Order.OrderStatus.CERRADO
                || order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se pueden modificar detalles de un pedido cerrado o cancelado");
        }

        // Actualizar cantidad
        if (detailData.getAmount() != null && detailData.getAmount() > 0) {
            detail.setAmount(detailData.getAmount());
        }

        // Actualizar notas
        if (detailData.getNotes() != null) {
            detail.setNotes(detailData.getNotes());
        }

        // Recalcular subtotal
        detail.calculateSubtotal();

        OrderDetail updated = detailRepository.save(detail);
        log.info("Detalle actualizado con ID: {}", id);

        // Actualizar totales del pedido
        updateOrderTotals(order.getIdOrder());

        return updated;
    }

    @Transactional
    public void removeDetailFromOrder(Integer id) {
        log.info("Eliminando detalle con ID: {}", id);

        OrderDetail detail = findById(id);
        Order order = detail.getOrder();

        // No permitir eliminar si el pedido está cerrado o cancelado
        if (order.getOrderStatus() == Order.OrderStatus.CERRADO
                || order.getOrderStatus() == Order.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se pueden eliminar detalles de un pedido cerrado o cancelado");
        }

        detailRepository.deleteById(id);
        log.info("Detalle eliminado");

        // Actualizar totales del pedido
        updateOrderTotals(order.getIdOrder());
    }

    /**
     * Actualiza automáticamente los totales del pedido basado en sus detalles
     */
    @Transactional
    public void updateOrderTotals(Integer orderId) {
        log.debug("Actualizando totales del pedido: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + orderId));

        // Calcular total de items
        Integer totalItems = detailRepository.sumAmountByOrderId(orderId);
        if (totalItems == null) {
            totalItems = 0;
        }

        // Calcular total del pedido
        BigDecimal total = detailRepository.sumSubtotalByOrderId(orderId);
        if (total == null) {
            total = BigDecimal.ZERO;
        }

        // Validar que hay al menos un item
        if (totalItems == 0) {
            throw new RuntimeException("El pedido debe tener al menos 1 item");
        }

        // Actualizar orden
        order.setTotalItems(totalItems);
        order.setTotal(total);

        orderRepository.save(order);
        log.info("Totales actualizados - Items: {}, Total: {}", totalItems, total);
    }

    /**
     * Obtiene el precio según el tipo de consumo
     */
    private BigDecimal getPriceByConsumptionType(Presentation presentation, Order.ConsumptionType type) {
        return switch (type) {
            case DELIVERY ->
                presentation.getDeliveryPrice() != null ? presentation.getDeliveryPrice() : presentation.getPrice();
            case TAKEOUT ->
                presentation.getTakeoutPrice() != null ? presentation.getTakeoutPrice() : presentation.getPrice();
            case PROMO -> presentation.getPromoPrice() != null ? presentation.getPromoPrice() : presentation.getPrice();
            case LOCAL -> presentation.getPrice();
        };
    }

    @Transactional(readOnly = true)
    public Integer getTotalItems(Integer orderId) {
        log.debug("Obteniendo total de items del pedido: {}", orderId);
        Integer total = detailRepository.sumAmountByOrderId(orderId);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(Integer orderId) {
        log.debug("Obteniendo total del pedido: {}", orderId);
        BigDecimal total = detailRepository.sumSubtotalByOrderId(orderId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countDetailsByOrder(Integer orderId) {
        log.debug("Contando detalles del pedido: {}", orderId);
        return detailRepository.countByOrderId(orderId);
    }
}