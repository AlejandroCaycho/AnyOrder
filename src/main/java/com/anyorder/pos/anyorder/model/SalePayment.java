package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    // ✅ Mantener @JsonIgnore para evitar referencia circular Sale -> SalePayment ->
    // Sale
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SALE", nullable = false)
    private Sale sale;

    @NotNull(message = "El tipo de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE", nullable = false)
    private PaymentType paymentType;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // Campos para EFECTIVO
    @DecimalMin(value = "0.0", message = "El efectivo recibido debe ser mayor o igual a 0")
    @Column(name = "CASH_RECEIVED", precision = 10, scale = 2)
    private BigDecimal cashReceived;

    @DecimalMin(value = "0.0", message = "El vuelto debe ser mayor o igual a 0")
    @Column(name = "CASH_CHANGE", precision = 10, scale = 2)
    private BigDecimal cashChange;

    // Campos para TARJETA
    @Size(max = 20, message = "El tipo de tarjeta no puede exceder 20 caracteres")
    @Column(name = "CARD_TYPE", length = 20)
    private String cardType;

    @Size(min = 4, max = 4, message = "Los últimos 4 dígitos deben ser exactamente 4")
    @Column(name = "CARD_LAST4", length = 4)
    private String cardLast4;

    @Size(max = 50, message = "La operación de tarjeta no puede exceder 50 caracteres")
    @Column(name = "CARD_OPERATION", length = 50)
    private String cardOperation;

    @Size(max = 50, message = "La referencia POS no puede exceder 50 caracteres")
    @Column(name = "POS_REFERENCE", length = 50)
    private String posReference;

    // Campos para YAPE/PLIN
    @Size(min = 9, message = "El teléfono debe tener al menos 9 dígitos")
    @Column(name = "PHONE_PAYMENT", length = 20)
    private String phonePayment;

    @Size(max = 50, message = "El código de transacción no puede exceder 50 caracteres")
    @Column(name = "TRANSACTION_CODE", length = 50)
    private String transactionCode;

    // Campos para TRANSFERENCIA
    @Size(max = 50, message = "El nombre del banco no puede exceder 50 caracteres")
    @Column(name = "BANK_NAME", length = 50)
    private String bankName;

    @Size(max = 30, message = "La cuenta bancaria no puede exceder 30 caracteres")
    @Column(name = "BANK_ACCOUNT", length = 30)
    private String bankAccount;

    @Size(max = 50, message = "La operación bancaria no puede exceder 50 caracteres")
    @Column(name = "BANK_OPERATION", length = 50)
    private String bankOperation;

    // URL de comprobante de pago (captura/foto)
    @Size(max = 500, message = "La URL del comprobante no puede exceder 500 caracteres")
    @Column(name = "PAYMENT_PROOF_URL", length = 500)
    private String paymentProofUrl;

    @Column(name = "PAYMENT_DATE", nullable = false, updatable = false)
    private LocalDateTime paymentDate;

    @PrePersist
    protected void onCreate() {
        this.paymentDate = LocalDateTime.now();

        // Validar campos según tipo de pago
        validatePaymentFields();
    }

    @PreUpdate
    protected void onUpdate() {
        validatePaymentFields();
    }

    private void validatePaymentFields() {
        if (this.paymentType == PaymentType.EFECTIVO) {
            if (this.cashReceived == null) {
                throw new RuntimeException("Para pago en EFECTIVO, debe especificar el monto recibido");
            }
            if (this.cashReceived.compareTo(this.amount) < 0) {
                throw new RuntimeException("El efectivo recibido no puede ser menor al monto a pagar");
            }
            // Calcular vuelto
            this.cashChange = this.cashReceived.subtract(this.amount);
        }
    }

    public enum PaymentType {
        EFECTIVO,
        TARJETA,
        YAPE,
        PLIN,
        TRANSFERENCIA
    }
}