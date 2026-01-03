package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.SalePayment;
import com.anyorder.pos.anyorder.service.SalePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sale-payments")
@RequiredArgsConstructor
@Tag(name = "Pagos de Ventas", description = "Gestión de métodos de pago de ventas (EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA)")
public class SalePaymentController {

    private final SalePaymentService salePaymentService;

    @Operation(summary = "Listar todos los pagos")
    @GetMapping
    public ResponseEntity<List<SalePayment>> getAll() {
        return ResponseEntity.ok(salePaymentService.findAll());
    }

    @Operation(summary = "Obtener pago por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SalePayment> getById(
            @Parameter(description = "ID del pago") @PathVariable Integer id) {
        return ResponseEntity.ok(salePaymentService.findById(id));
    }

    @Operation(summary = "Obtener pagos de una venta", description = "Lista todos los métodos de pago usados en una venta específica")
    @GetMapping("/sale/{saleId}")
    public ResponseEntity<List<SalePayment>> getBySaleId(
            @Parameter(description = "ID de la venta") @PathVariable Integer saleId) {
        return ResponseEntity.ok(salePaymentService.findBySaleId(saleId));
    }

    @Operation(summary = "Obtener pagos por tipo", description = "Lista pagos filtrados por método (EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA)")
    @GetMapping("/type/{paymentType}")
    public ResponseEntity<List<SalePayment>> getByPaymentType(
            @PathVariable SalePayment.PaymentType paymentType) {
        return ResponseEntity.ok(salePaymentService.findByPaymentType(paymentType));
    }

    @Operation(summary = "Pagos por tipo y rango de fechas")
    @GetMapping("/type/{paymentType}/date-range")
    public ResponseEntity<List<SalePayment>> getByTypeAndDateRange(
            @PathVariable SalePayment.PaymentType paymentType,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(salePaymentService.findByPaymentTypeAndDateRange(paymentType, startDate, endDate));
    }

    @Operation(summary = "Total pagado en una venta")
    @GetMapping("/sale/{saleId}/total")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(@PathVariable Integer saleId) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalAmount", salePaymentService.getTotalAmount(saleId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar métodos de pago de una venta")
    @GetMapping("/sale/{saleId}/count")
    public ResponseEntity<Map<String, Long>> countPayments(@PathVariable Integer saleId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", salePaymentService.countPaymentsBySale(saleId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Sumar total por tipo de pago y rango de fechas", description = "Para reportes de caja: cuánto se recaudó en efectivo, tarjeta, etc.")
    @GetMapping("/sum/type/{paymentType}/date-range")
    public ResponseEntity<Map<String, BigDecimal>> sumByTypeAndDateRange(
            @PathVariable SalePayment.PaymentType paymentType,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", salePaymentService.sumByPaymentTypeAndDateRange(paymentType, startDate, endDate));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resumen de pagos del día por tipo", description = "Devuelve cuánto se recaudó HOY en cada método de pago")
    @GetMapping("/today/summary")
    public ResponseEntity<List<Object[]>> getTodaySummary() {
        return ResponseEntity.ok(salePaymentService.getTodayPaymentsByType());
    }

    @Operation(summary = "Contar pagos por tipo", description = "Total de transacciones realizadas con un método específico")
    @GetMapping("/count/type/{paymentType}")
    public ResponseEntity<Map<String, Long>> countByType(@PathVariable SalePayment.PaymentType paymentType) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", salePaymentService.countByPaymentType(paymentType));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Buscar por código de transacción", description = "Para YAPE, PLIN o TRANSFERENCIA")
    @GetMapping("/transaction/{transactionCode}")
    public ResponseEntity<List<SalePayment>> getByTransactionCode(@PathVariable String transactionCode) {
        return ResponseEntity.ok(salePaymentService.findByTransactionCode(transactionCode));
    }

    @Operation(summary = "Buscar por operación bancaria", description = "Para TRANSFERENCIA bancaria")
    @GetMapping("/bank-operation/{bankOperation}")
    public ResponseEntity<List<SalePayment>> getByBankOperation(@PathVariable String bankOperation) {
        return ResponseEntity.ok(salePaymentService.findByBankOperation(bankOperation));
    }
}