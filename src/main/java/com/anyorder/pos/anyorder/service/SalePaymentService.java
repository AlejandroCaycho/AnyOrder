package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.SalePayment;
import com.anyorder.pos.anyorder.repository.SalePaymentRepository;
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
public class SalePaymentService {

    private final SalePaymentRepository salePaymentRepository;

    @Transactional(readOnly = true)
    public List<SalePayment> findAll() {
        log.debug("Obteniendo todos los pagos");
        return salePaymentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SalePayment findById(Integer id) {
        log.debug("Buscando pago con ID: {}", id);
        return salePaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalePayment> findBySaleId(Integer saleId) {
        log.debug("Obteniendo pagos de la venta: {}", saleId);
        return salePaymentRepository.findPaymentsBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public List<SalePayment> findByPaymentType(SalePayment.PaymentType paymentType) {
        log.debug("Obteniendo pagos por tipo: {}", paymentType);
        return salePaymentRepository.findByPaymentType(paymentType);
    }

    @Transactional(readOnly = true)
    public List<SalePayment> findByPaymentTypeAndDateRange(
            SalePayment.PaymentType paymentType,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Obteniendo pagos tipo {} entre {} y {}", paymentType, startDate, endDate);
        return salePaymentRepository.findByPaymentTypeAndDateRange(paymentType, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmount(Integer saleId) {
        log.debug("Obteniendo total de pagos de la venta: {}", saleId);
        BigDecimal total = salePaymentRepository.sumAmountBySaleId(saleId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countPaymentsBySale(Integer saleId) {
        log.debug("Contando pagos de la venta: {}", saleId);
        return salePaymentRepository.countBySaleId(saleId);
    }

    @Transactional(readOnly = true)
    public BigDecimal sumByPaymentTypeAndDateRange(
            SalePayment.PaymentType paymentType,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Sumando pagos tipo {} entre {} y {}", paymentType, startDate, endDate);
        BigDecimal total = salePaymentRepository.sumByPaymentTypeAndDateRange(paymentType, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getTodayPaymentsByType() {
        log.debug("Obteniendo totales del día por tipo de pago");
        return salePaymentRepository.sumTodayByPaymentType();
    }

    @Transactional(readOnly = true)
    public long countByPaymentType(SalePayment.PaymentType paymentType) {
        log.debug("Contando pagos tipo: {}", paymentType);
        return salePaymentRepository.countByPaymentType(paymentType);
    }

    @Transactional(readOnly = true)
    public List<SalePayment> findByTransactionCode(String transactionCode) {
        log.debug("Buscando pago por código de transacción: {}", transactionCode);
        return salePaymentRepository.findByTransactionCode(transactionCode);
    }

    @Transactional(readOnly = true)
    public List<SalePayment> findByBankOperation(String bankOperation) {
        log.debug("Buscando pago por operación bancaria: {}", bankOperation);
        return salePaymentRepository.findByBankOperation(bankOperation);
    }
}