package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.SalePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalePaymentRepository extends JpaRepository<SalePayment, Integer> {

    // Buscar pagos por ID de venta
    List<SalePayment> findBySale_IdSale(Integer saleId);

    // Buscar pagos por tipo de pago
    List<SalePayment> findByPaymentType(SalePayment.PaymentType paymentType);

    // Obtener pagos de una venta ordenados por fecha
    @Query("SELECT sp FROM SalePayment sp WHERE sp.sale.idSale = :saleId ORDER BY sp.paymentDate ASC")
    List<SalePayment> findPaymentsBySaleId(@Param("saleId") Integer saleId);

    // Sumar total de pagos de una venta
    @Query("SELECT SUM(sp.amount) FROM SalePayment sp WHERE sp.sale.idSale = :saleId")
    BigDecimal sumAmountBySaleId(@Param("saleId") Integer saleId);

    // Contar pagos de una venta
    @Query("SELECT COUNT(sp) FROM SalePayment sp WHERE sp.sale.idSale = :saleId")
    long countBySaleId(@Param("saleId") Integer saleId);

    // Pagos por tipo en un rango de fechas
    @Query("SELECT sp FROM SalePayment sp WHERE sp.paymentType = :paymentType AND sp.paymentDate BETWEEN :startDate AND :endDate ORDER BY sp.paymentDate DESC")
    List<SalePayment> findByPaymentTypeAndDateRange(
            @Param("paymentType") SalePayment.PaymentType paymentType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Sumar total por tipo de pago en un rango de fechas
    @Query("SELECT SUM(sp.amount) FROM SalePayment sp WHERE sp.paymentType = :paymentType AND sp.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumByPaymentTypeAndDateRange(
            @Param("paymentType") SalePayment.PaymentType paymentType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Pagos del día por tipo
    @Query("SELECT sp.paymentType, SUM(sp.amount) FROM SalePayment sp WHERE DATE(sp.paymentDate) = CURRENT_DATE GROUP BY sp.paymentType")
    List<Object[]> sumTodayByPaymentType();

    // Contar pagos por tipo
    @Query("SELECT COUNT(sp) FROM SalePayment sp WHERE sp.paymentType = :paymentType")
    long countByPaymentType(@Param("paymentType") SalePayment.PaymentType paymentType);

    // Buscar por código de transacción (YAPE, PLIN, TRANSFERENCIA)
    @Query("SELECT sp FROM SalePayment sp WHERE sp.transactionCode = :code")
    List<SalePayment> findByTransactionCode(@Param("code") String transactionCode);

    // Buscar por operación bancaria
    @Query("SELECT sp FROM SalePayment sp WHERE sp.bankOperation = :operation")
    List<SalePayment> findByBankOperation(@Param("operation") String bankOperation);
}