package com.anyorder.pos.anyorder.modules.sales.service;

import com.anyorder.pos.anyorder.modules.sales.model.DetailSale;
import com.anyorder.pos.anyorder.modules.sales.model.Sale;
import com.anyorder.pos.anyorder.modules.sales.model.SalePayment;
import com.anyorder.pos.anyorder.modules.sales.repository.DetailSaleRepository;
import com.anyorder.pos.anyorder.modules.sales.repository.SalePaymentRepository;
import com.anyorder.pos.anyorder.modules.sales.repository.SaleRepository;
import com.anyorder.pos.anyorder.modules.orders.model.Order;
import com.anyorder.pos.anyorder.modules.orders.model.OrderDetail;
import com.anyorder.pos.anyorder.modules.orders.repository.OrderDetailRepository;
import com.anyorder.pos.anyorder.modules.orders.repository.OrderRepository;
import com.anyorder.pos.anyorder.modules.customers.service.CustomerService;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de ventas que gestiona el ciclo de cobro de pedidos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final DetailSaleRepository detailSaleRepository;
    private final SalePaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerService customerService;
    private final TablesService tablesService;


    @Transactional(readOnly = true)
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Sale findById(Integer id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Sale> findByUser(Integer userId) {
        return saleRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByCustomer(Integer customerId) {
        return saleRepository.findByCustomer_IdCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public List<Sale> findToday() {
        return saleRepository.findTodaySales(LocalDate.now().atStartOfDay());
    }

    @Transactional(readOnly = true)
    public List<Sale> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTodayTotal() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusNanos(1);
        return saleRepository.sumTotalByDateRange(start, end);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMostSoldPresentations() {
        return saleRepository.findMostSoldPresentations();
    }


    /**
     * Crea una venta para un pedido específico (ID de orden en sale.order).
     * El pedido debe estar en estado ENTREGADO.
     *
     * @param sale     encabezado de la venta (user, order, customer opcionales)
     * @param payments lista de pagos (puede ser múltiple/mixto)
     */
    @Transactional
    public Sale createFromOrder(Sale sale, List<SalePayment> payments) {
        log.info("Procesando venta para pedido #{}", sale.getOrder().getIdOrder());

        Order order = orderRepository.findById(sale.getOrder().getIdOrder())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        validateOrderForSale(order);
        validatePayments(payments, order.getTotal());

        // Heredar cliente del pedido si no se especificó
        if (sale.getCustomer() == null && order.getCustomer() != null) {
            sale.setCustomer(order.getCustomer());
        }

        Sale saved = saveSale(sale, order, payments);

        closeOrder(order);

        return saved;
    }


    /**
     * Cobra todos los pedidos ENTREGADOS de un cliente en una sola venta.
     * Útil para mesas donde el cliente hizo varios pedidos durante la sesión.
     *
     * @param sale     encabezado con user y customer
     * @param payments lista de pagos
     */
    @Transactional
    public Sale createFromCustomerOrders(Sale sale, List<SalePayment> payments) {
        Integer customerId = sale.getCustomer().getIdCustomer();
        log.info("Procesando venta agrupada para cliente #{}", customerId);

        List<Order> orders = orderRepository.findByCustomer_IdCustomerAndOrderStatus(
                customerId, Order.OrderStatus.ENTREGADO);

        if (orders.isEmpty()) {
            throw new RuntimeException("No hay pedidos ENTREGADOS para el cliente ID: " + customerId);
        }

        // Calcular total consolidado
        BigDecimal grandTotal = orders.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        validatePayments(payments, grandTotal);

        sale.setTotal(grandTotal);
        sale.setState(1);
        Sale savedSale = saleRepository.save(sale);

        // Copiar detalles de todos los pedidos
        for (Order order : orders) {
            copyOrderDetails(savedSale, order);
            closeOrder(order);
        }

        // Registrar pagos
        for (SalePayment payment : payments) {
            payment.validate();
            payment.setSale(savedSale);
            paymentRepository.save(payment);
        }

        // Actualizar métricas del cliente
        customerService.updatePurchaseInfo(customerId, grandTotal);

        log.info("Venta agrupada #{} creada. Total: {}, Órdenes: {}", savedSale.getIdSale(), grandTotal, orders.size());
        return savedSale;
    }


    /**
     * Anula una venta VIGENTE. Cambia state a 0 (ANULADA).
     * No devuelve stock automáticamente (debe hacerse como ajuste manual).
     */
    @Transactional
    public Sale annul(Integer id) {
        Sale sale = findById(id);

        if (sale.getState() != 1) {
            throw new RuntimeException(
                    "Solo se puede anular una venta VIGENTE. Estado actual: " + sale.getStateDescription());
        }

        sale.setState(0);
        log.info("Venta #{} anulada", id);
        return saleRepository.save(sale);
    }

    private void validateOrderForSale(Order order) {
        if (order.getOrderStatus() != Order.OrderStatus.ENTREGADO) {
            throw new RuntimeException(
                    "Solo se pueden cobrar pedidos ENTREGADOS. Estado actual: " + order.getOrderStatus());
        }

        if (saleRepository.existsByOrder_IdOrder(order.getIdOrder())) {
            throw new RuntimeException("El pedido #" + order.getIdOrder() + " ya tiene una venta registrada");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_IdOrder(order.getIdOrder());
        if (details.isEmpty()) {
            throw new RuntimeException("El pedido #" + order.getIdOrder() + " no tiene detalles");
        }
    }

    private void validatePayments(List<SalePayment> payments, BigDecimal expectedTotal) {
        if (payments == null || payments.isEmpty()) {
            throw new RuntimeException("Debe proporcionar al menos un pago");
        }

        payments.forEach(SalePayment::validate);

        BigDecimal totalPaid = payments.stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(expectedTotal) < 0) {
            throw new RuntimeException(
                    "Pago insuficiente. Total: " + expectedTotal + ", Pagado: " + totalPaid);
        }
    }

    private Sale saveSale(Sale sale, Order order, List<SalePayment> payments) {
        sale.setOrder(order);
        sale.setTotal(order.getTotal());
        sale.setState(1);
        Sale savedSale = saleRepository.save(sale);

        copyOrderDetails(savedSale, order);

        for (SalePayment payment : payments) {
            payment.validate();
            payment.setSale(savedSale);
            paymentRepository.save(payment);
        }

        if (order.getCustomer() != null) {
            customerService.updatePurchaseInfo(order.getCustomer().getIdCustomer(), order.getTotal());
        }

        log.info("Venta #{} creada. Total: {}", savedSale.getIdSale(), order.getTotal());
        return savedSale;
    }

    private void copyOrderDetails(Sale sale, Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_IdOrder(order.getIdOrder());

        for (OrderDetail od : orderDetails) {
            DetailSale ds = new DetailSale();
            ds.setSale(sale);
            ds.setPresentation(od.getPresentation());
            ds.setAmount(od.getAmount());
            ds.setUnitPrice(od.getUnitPrice());
            ds.setSubtotal(od.getSubtotal());
            detailSaleRepository.save(ds);
        }
    }

    private void closeOrder(Order order) {
        order.setOrderStatus(Order.OrderStatus.CERRADO);
        orderRepository.save(order);

        // Liberar mesa si es consumo local
        if (order.getTypeOfConsumption() == Order.ConsumptionType.LOCAL
                && order.getTable() != null
                && order.getNumberOfPeople() != null) {
            try {
                tablesService.decrementOccupancy(order.getTable().getIdTable(), order.getNumberOfPeople());
            } catch (Exception e) {
                log.warn("No se pudo liberar la mesa #{}: {}", order.getTable().getIdTable(), e.getMessage());
            }
        }
    }
}
