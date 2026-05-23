package com.anyorder.pos.anyorder.modules.sales.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Un pago individual dentro de una venta.
 * Una venta puede tener múltiples pagos (pago mixto: efectivo + tarjeta, etc.)
 *
 * Alineado 100% con la tabla SALE_PAYMENTS del SQL.
 */
@Entity
@Table(name = "SALE_PAYMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PAYMENT")
    private Integer idPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SALE", nullable = false)
    @JsonBackReference("sale-payments")
    private Sale sale;

    @NotNull(message = "El tipo de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE", nullable = false)
    private PaymentType paymentType;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01")
    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // ── EFECTIVO ────────────────────────────────────────────
    @Column(name = "CASH_RECEIVED", precision = 10, scale = 2)
    private BigDecimal cashReceived;

    @Column(name = "CASH_CHANGE", precision = 10, scale = 2)
    private BigDecimal cashChange;

    // ── TARJETA ─────────────────────────────────────────────
    @Column(name = "CARD_TYPE", length = 20)
    private String cardType; // VISA, MASTERCARD, AMEX, etc.

    @Column(name = "CARD_LAST4", length = 4)
    private String cardLast4;

    @Column(name = "CARD_OPERATION", length = 50)
    private String cardOperation;

    @Column(name = "POS_REFERENCE", length = 50)
    private String posReference;

    // ── YAPE / PLIN ─────────────────────────────────────────
    @Column(name = "PHONE_PAYMENT", length = 20)
    private String phonePayment;

    @Column(name = "TRANSACTION_CODE", length = 50)
    private String transactionCode;

    // ── TRANSFERENCIA ────────────────────────────────────────
    @Column(name = "BANK_NAME", length = 50)
    private String bankName;

    @Column(name = "BANK_ACCOUNT", length = 30)
    private String bankAccount;

    @Column(name = "BANK_OPERATION", length = 50)
    private String bankOperation;

    // ── COMPROBANTE ──────────────────────────────────────────
    @Column(name = "PAYMENT_PROOF_URL", length = 500)
    private String paymentProofUrl;

    @CreationTimestamp
    @Column(name = "PAYMENT_DATE", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentDate;

    public enum PaymentType {
        EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA
    }

    /** Calcula y asigna el vuelto si es pago en efectivo */
    @PrePersist
    public void computeChange() {
        if (paymentType == PaymentType.EFECTIVO && cashReceived != null && cashChange == null && amount != null) {
            cashChange = cashReceived.subtract(amount);
        }
    }

    /** Validación de datos por tipo de pago */
    public void validate() {
        if (paymentType == null) throw new IllegalArgumentException("El tipo de pago es obligatorio");

        switch (paymentType) {
            case EFECTIVO -> {
                if (cashReceived == null) throw new IllegalArgumentException("EFECTIVO requiere monto recibido");
                if (cashReceived.compareTo(amount) < 0) throw new IllegalArgumentException("El efectivo recibido no puede ser menor al monto");
            }
            case TARJETA -> {
                if (cardLast4 == null || cardLast4.length() != 4) throw new IllegalArgumentException("TARJETA requiere los últimos 4 dígitos");
                if (cardOperation == null || cardOperation.isBlank()) throw new IllegalArgumentException("TARJETA requiere número de operación");
            }
            case YAPE, PLIN -> {
                if (phonePayment == null || phonePayment.isBlank()) throw new IllegalArgumentException(paymentType + " requiere número de teléfono");
                if (transactionCode == null || transactionCode.isBlank()) throw new IllegalArgumentException(paymentType + " requiere código de transacción");
            }
            case TRANSFERENCIA -> {
                if (bankName == null || bankName.isBlank()) throw new IllegalArgumentException("TRANSFERENCIA requiere nombre del banco");
                if (bankOperation == null || bankOperation.isBlank()) throw new IllegalArgumentException("TRANSFERENCIA requiere número de operación");
            }
        }
    }
}
