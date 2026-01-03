package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.PurchaseOrder;
import com.anyorder.pos.anyorder.model.PurchaseOrderDetail;
import com.anyorder.pos.anyorder.model.Ingredients;
import com.anyorder.pos.anyorder.repository.PurchaseOrderRepository;
import com.anyorder.pos.anyorder.repository.PurchaseOrderDetailRepository;
import com.anyorder.pos.anyorder.repository.IngredientsRepository;
import com.anyorder.pos.anyorder.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository detailRepository;
    private final IngredientsRepository ingredientsRepository;
    private final SupplierService supplierService;
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        log.debug("Obteniendo todas las órdenes de compra");
        return purchaseOrderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findById(Integer id) {
        log.debug("Buscando orden de compra con ID: {}", id);
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findByOrderNumber(String orderNumber) {
        log.debug("Buscando orden de compra con número: {}", orderNumber);
        return purchaseOrderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada con número: " + orderNumber));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findBySupplier(Integer supplierId) {
        log.debug("Obteniendo órdenes del proveedor: {}", supplierId);
        return purchaseOrderRepository.findBySupplier_IdSupplier(supplierId);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findByUser(Integer userId) {
        log.debug("Obteniendo órdenes del usuario: {}", userId);
        return purchaseOrderRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findByOrderStatus(PurchaseOrder.OrderStatus status) {
        log.debug("Obteniendo órdenes con estado: {}", status);
        return purchaseOrderRepository.findByOrderStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findByPaymentStatus(PurchaseOrder.PaymentStatus status) {
        log.debug("Obteniendo órdenes con estado de pago: {}", status);
        return purchaseOrderRepository.findByPaymentStatus(status);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo órdenes entre {} y {}", startDate, endDate);
        return purchaseOrderRepository.findByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findPendingOrders() {
        log.debug("Obteniendo órdenes pendientes");
        return purchaseOrderRepository.findPendingOrders();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findOverdueOrders() {
        log.debug("Obteniendo órdenes vencidas");
        return purchaseOrderRepository.findOverdueOrders();
    }

    /**
     * Crear orden de compra con detalles
     */
    @Transactional
    public PurchaseOrder create(PurchaseOrder purchaseOrder) {
        log.info("Creando nueva orden de compra");

        // Validaciones
        if (purchaseOrder.getUser() == null || purchaseOrder.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }

        if (purchaseOrder.getSupplier() == null || purchaseOrder.getSupplier().getIdSupplier() == null) {
            throw new RuntimeException("El proveedor es obligatorio");
        }

        if (purchaseOrder.getDeliveryDate() == null) {
            throw new RuntimeException("La fecha de entrega es obligatoria");
        }

        if (purchaseOrder.getDetailsList() == null || purchaseOrder.getDetailsList().isEmpty()) {
            throw new RuntimeException("La orden debe tener al menos un detalle");
        }

        // Validar número de orden único
        if (purchaseOrderRepository.existsByOrderNumber(purchaseOrder.getOrderNumber())) {
            throw new RuntimeException("Ya existe una orden con el número: " + purchaseOrder.getOrderNumber());
        }

        // Validar usuario existe
        usersRepository.findById(purchaseOrder.getUser().getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar proveedor existe
        supplierService.findById(purchaseOrder.getSupplier().getIdSupplier());

        // Inicializar
        purchaseOrder.setTotal(BigDecimal.ZERO);
        purchaseOrder.setOrderStatus(PurchaseOrder.OrderStatus.PENDIENTE);
        purchaseOrder.setPaymentStatus(PurchaseOrder.PaymentStatus.PENDIENTE);

        // Guardar orden
        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Orden de compra creada con ID: {}", saved.getIdPurchaseOrder());

        // Procesar detalles
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderDetail detail : purchaseOrder.getDetailsList()) {
            Ingredients ingredient = ingredientsRepository.findById(detail.getIngredient().getIdIngredient())
                    .orElseThrow(() -> new RuntimeException("Ingrediente no encontrado"));

            detail.setPurchaseOrder(saved);
            detail.setIngredient(ingredient);
            detail.calculateSubtotal();
            detail.setReceivedQuantity(BigDecimal.ZERO);

            detailRepository.save(detail);
            totalAmount = totalAmount.add(detail.getSubtotal());
        }

        // Actualizar total
        saved.setTotal(totalAmount);
        purchaseOrderRepository.save(saved);

        log.info("Orden creada con total: {}", totalAmount);
        return saved;
    }

    /**
     * Actualizar orden de compra
     */
    @Transactional
    public PurchaseOrder update(Integer id, PurchaseOrder orderData) {
        log.info("Actualizando orden de compra: {}", id);

        PurchaseOrder order = findById(id);

        // No permitir cambios si está recibida o cancelada
        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO ||
                order.getOrderStatus() == PurchaseOrder.OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede modificar una orden recibida o cancelada");
        }

        if (orderData.getDeliveryDate() != null) {
            order.setDeliveryDate(orderData.getDeliveryDate());
        }

        if (orderData.getOrderStatus() != null) {
            order.setOrderStatus(orderData.getOrderStatus());
        }

        if (orderData.getPaymentStatus() != null) {
            order.setPaymentStatus(orderData.getPaymentStatus());
        }

        if (orderData.getNotes() != null) {
            order.setNotes(orderData.getNotes());
        }

        return purchaseOrderRepository.save(order);
    }

    /**
     * Marcar orden como recibida y actualizar inventario
     */
    @Transactional
    public PurchaseOrder markAsReceived(Integer id) {
        log.info("Marcando orden {} como recibida", id);

        PurchaseOrder order = findById(id);

        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO) {
            throw new RuntimeException("La orden ya está marcada como recibida");
        }

        // Actualizar inventario
        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderId(id);
        for (PurchaseOrderDetail detail : details) {
            Ingredients ingredient = detail.getIngredient();
            BigDecimal newQuantity = ingredient.getQuantity().add(detail.getQuantity());
            ingredient.setQuantity(newQuantity);
            ingredientsRepository.save(ingredient);

            // Marcar detalle como recibido
            detail.setReceivedQuantity(detail.getQuantity());
            detailRepository.save(detail);
        }

        order.marcarComoRecibida();
        PurchaseOrder updated = purchaseOrderRepository.save(order);

        log.info("Orden {} recibida e inventario actualizado", id);
        return updated;
    }

    /**
     * Cancelar orden
     */
    @Transactional
    public void cancelOrder(Integer id) {
        log.info("Cancelando orden: {}", id);

        PurchaseOrder order = findById(id);

        if (order.getOrderStatus() == PurchaseOrder.OrderStatus.RECIBIDO) {
            throw new RuntimeException("No se puede cancelar una orden ya recibida");
        }

        order.setOrderStatus(PurchaseOrder.OrderStatus.CANCELADO);
        purchaseOrderRepository.save(order);

        log.info("Orden {} cancelada", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTotalByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Sumando totales entre {} y {}", startDate, endDate);
        BigDecimal total = purchaseOrderRepository.sumTotalByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countByStatus(PurchaseOrder.OrderStatus status) {
        log.debug("Contando órdenes con estado: {}", status);
        return purchaseOrderRepository.countByOrderStatus(status);
    }
}