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

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SaleRepository saleRepository;
    private final DetailSaleRepository detailSaleRepository;
    private final SalePaymentRepository salePaymentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerService customerService;
    private final TablesService tablesService;

    @Transactional
    public Sale create(Sale sale) {
        log.info("Creando venta desde pedido confirmado");

        if (sale.getUser() == null || sale.getUser().getIdUser() == null) {
            throw new RuntimeException("El usuario es obligatorio");
        }

        if (sale.getOrder() == null || sale.getOrder().getIdOrder() == null) {
            throw new RuntimeException("El pedido es obligatorio");
        }

        Order order = orderRepository.findById(sale.getOrder().getIdOrder())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!order.getConfirmed()) {
            throw new RuntimeException(
                    "El pedido debe estar CONFIRMADO antes de crear la venta. " +
                            "El stock se descuenta en la confirmacion del pedido.");
        }

        if (saleRepository.existsByOrder_IdOrder(order.getIdOrder())) {
            throw new RuntimeException("El pedido ya tiene una venta registrada");
        }

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_IdOrder(order.getIdOrder());
        if (orderDetails == null || orderDetails.isEmpty()) {
            throw new RuntimeException("El pedido no tiene items");
        }

        if (sale.getPayments() == null || sale.getPayments().isEmpty()) {
            throw new RuntimeException("La venta debe tener al menos 1 metodo de pago");
        }

        distribuirPagos(sale.getPayments(), order.getTotal());

        BigDecimal totalPayments = sale.getPayments().stream()
                .map(SalePayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPayments.compareTo(order.getTotal()) < 0) {
            throw new RuntimeException(
                    "Pago insuficiente. Total: " + order.getTotal() +
                            ", Recibido: " + totalPayments);
        }

        BigDecimal cambio = totalPayments.subtract(order.getTotal());
        if (cambio.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Vuelto al cliente: {}", cambio);
        }

        if (sale.getCustomer() == null && order.getCustomer() != null) {
            sale.setCustomer(order.getCustomer());
            log.info("Customer copiado desde pedido: {}", order.getCustomer().getIdCustomer());
        }

        if (sale.getState() == null) {
            sale.setState(2);
        }

        Sale savedSale = saleRepository.save(sale);
        log.info("Venta creada con ID: {}", savedSale.getIdSale());

        copiarDetallesDelPedido(savedSale, orderDetails);
        guardarPagos(savedSale, sale.getPayments());

        order.setOrderStatus(Order.OrderStatus.CERRADO);
        orderRepository.save(order);
        log.info("Pedido {} marcado como CERRADO", order.getIdOrder());

        if (order.getConsumptionType() == Order.ConsumptionType.LOCAL &&
                order.getTable() != null) {
            liberarMesa(order.getTable().getIdTable());
        }

        if (order.getCustomer() != null && order.getCustomer().getIdCustomer() != null) {
            actualizarEstadisticasCliente(order.getCustomer().getIdCustomer(), order.getTotal());
        }

        log.info("Venta completada exitosamente");
        return savedSale;
    }

    private void distribuirPagos(List<SalePayment> payments, BigDecimal totalOrder) {
        int paymentCount = payments.size();

        if (paymentCount == 1) {
            payments.get(0).setAmount(totalOrder);
            log.info("Pago unico: {}", totalOrder);
        } else {
            BigDecimal totalAsignado = BigDecimal.ZERO;

            for (int i = 0; i < paymentCount; i++) {
                SalePayment payment = payments.get(i);

                if (payment.getAmount() == null ||
                        payment.getAmount().compareTo(BigDecimal.ZERO) == 0) {

                    if (i == paymentCount - 1) {
                        payment.setAmount(totalOrder.subtract(totalAsignado));
                    } else {
                        BigDecimal montoProporcion = totalOrder.divide(
                                new BigDecimal(paymentCount),
                                2,
                                BigDecimal.ROUND_HALF_UP);
                        payment.setAmount(montoProporcion);
                    }
                }

                totalAsignado = totalAsignado.add(payment.getAmount());
            }

            log.info("{} pagos distribuidos. Total: {}", paymentCount, totalAsignado);
        }
    }

    private void copiarDetallesDelPedido(Sale sale, List<OrderDetail> orderDetails) {
        BigDecimal totalDetails = BigDecimal.ZERO;

        for (OrderDetail orderDetail : orderDetails) {
            DetailSale detailSale = new DetailSale();
            detailSale.setSale(sale);
            detailSale.setPresentation(orderDetail.getPresentation());
            detailSale.setAmount(orderDetail.getAmount());
            detailSale.setUnitPrice(orderDetail.getUnitPrice());
            detailSale.setSubtotal(orderDetail.getSubtotal());

            detailSaleRepository.save(detailSale);
            totalDetails = totalDetails.add(detailSale.getSubtotal());
        }

        log.info("Copiados {} detalles. Total: {}", orderDetails.size(), totalDetails);
    }

    private void guardarPagos(Sale sale, List<SalePayment> payments) {
        BigDecimal totalPayments = BigDecimal.ZERO;

        for (SalePayment payment : payments) {
            payment.setSale(sale);
            salePaymentRepository.save(payment);
            totalPayments = totalPayments.add(payment.getAmount());

            log.info("Pago registrado: {} - {}",
                    payment.getPaymentType(), payment.getAmount());
        }

        log.info("Total pagos guardados: {}", totalPayments);
    }

    private void liberarMesa(Integer tableId) {
        try {
            tablesService.releaseTable(tableId);
            log.info("Mesa {} liberada automaticamente", tableId);
        } catch (Exception e) {
            log.error("Error al liberar mesa {}: {}", tableId, e.getMessage());
        }
    }

    private void actualizarEstadisticasCliente(Integer customerId, BigDecimal amount) {
        try {
            customerService.updatePurchaseInfo(customerId, amount);
            log.info("Estadisticas del cliente {} actualizadas", customerId);
        } catch (Exception e) {
            log.error("Error actualizando cliente {}: {}", customerId, e.getMessage());
        }
    }

    @Transactional
    public void cancelSale(Integer id, String motivo) {
        log.info("Anulando venta {} - Motivo: {}", id, motivo);

        Sale sale = findById(id);

        if (sale.getState() == 0) {
            throw new RuntimeException("La venta ya esta anulada");
        }

        LocalDateTime limiteAnulacion = LocalDateTime.now().minusHours(24);
        if (sale.getOrder().getOrderDate() != null &&
                sale.getOrder().getOrderDate().isBefore(limiteAnulacion)) {
            throw new RuntimeException(
                    "No se puede anular una venta de mas de 24 horas. " +
                            "Contacte al administrador.");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new RuntimeException("Debe especificar el motivo de anulacion");
        }

        sale.setState(0);
        saleRepository.save(sale);

        log.warn("ANULACION: Considere devolver el stock manualmente si aplica");
        log.info("Venta {} anulada. Motivo: {}", id, motivo);
    }

    @Transactional(readOnly = true)
    public Sale findById(Integer id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Sale> findAll() {
        return saleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sale> findByUserId(Integer userId) {
        return saleRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByCustomerId(Integer customerId) {
        return saleRepository.findByCustomer_IdCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByOrderId(Integer orderId) {
        return saleRepository.findByOrder_IdOrder(orderId);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByState(Integer state) {
        return saleRepository.findByState(state);
    }

    @Transactional(readOnly = true)
    public List<Sale> findTodaySales() {
        return saleRepository.findTodaySales();
    }

    @Transactional(readOnly = true)
    public List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByStateAndDateRange(Integer state, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByStateAndDateRange(state, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Sale> findRecentSales(Integer limit) {
        return saleRepository.findRecentSales(limit);
    }

    @Transactional(readOnly = true)
    public List<Sale> findByUserAndDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTotalByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal total = saleRepository.sumTotalByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countByState(Integer state) {
        return saleRepository.countByState(state);
    }

    @Transactional
    public Sale update(Integer id, Sale saleData) {
        log.info("Actualizando venta con ID: {}", id);

        Sale sale = findById(id);

        if (saleData.getState() != null) {
            sale.setState(saleData.getState());
        }

        Sale updated = saleRepository.save(sale);
        log.info("Venta actualizada con ID: {}", id);

        return updated;
    }

    @Transactional(readOnly = true)
    public SaleStatistics getStatisticsToday() {
        List<Sale> todaySales = findTodaySales();

        BigDecimal totalSales = todaySales.stream()
                .filter(s -> s.getState() == 2)
                .map(s -> s.getOrder().getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long salesCount = todaySales.stream()
                .filter(s -> s.getState() == 2)
                .count();

        long canceledCount = todaySales.stream()
                .filter(s -> s.getState() == 0)
                .count();

        return new SaleStatistics(totalSales, salesCount, canceledCount);
    }

    public static class SaleStatistics {
        public final BigDecimal totalSales;
        public final long salesCount;
        public final long canceledCount;

        public SaleStatistics(BigDecimal totalSales, long salesCount, long canceledCount) {
            this.totalSales = totalSales;
            this.salesCount = salesCount;
            this.canceledCount = canceledCount;
        }
    }
}