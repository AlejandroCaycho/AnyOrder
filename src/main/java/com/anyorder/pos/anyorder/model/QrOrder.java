package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "QR_ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_QR_ORDER")
    private Integer idQrOrder;

    @NotNull(message = "La sesión QR es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SESSION", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idSession")
    @JsonIdentityReference(alwaysAsId = true)
    private QrSession session;

    @NotNull(message = "La presentación es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PRESENTATION", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idPresentation")
    @JsonIdentityReference(alwaysAsId = true)
    private Presentation presentation;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 100, message = "La cantidad no puede exceder 100")
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio unitario debe ser mayor a 0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = false, message = "El subtotal debe ser mayor a 0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @NotNull(message = "El estado del pedido es obligatorio")
    @Column(name = "ORDER_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.PENDIENTE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CONFIRMED_BY")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private Users confirmedBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CONFIRMED_AT")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.PENDIENTE;
        }
        calculateSubtotal();
        validateConfirmedDate();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateSubtotal();
        validateConfirmedDate();
    }

    private void calculateSubtotal() {
        if (this.unitPrice != null && this.amount != null) {
            this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.amount));
        }
    }

    private void validateConfirmedDate() {
        if (this.confirmedAt != null && this.createdAt != null) {
            if (this.confirmedAt.isBefore(this.createdAt)) {
                throw new IllegalArgumentException(
                        "La fecha de confirmación no puede ser anterior a la fecha de creación");
            }
        }
    }

    public enum OrderStatus {
        PENDIENTE,
        CONFIRMADO,
        CANCELADO
    }
}