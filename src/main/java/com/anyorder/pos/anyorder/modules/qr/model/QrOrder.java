package com.anyorder.pos.anyorder.modules.qr.model;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un pedido realizado desde una sesión QR.
 * Debe ser confirmado por el personal para integrarse al flujo normal de pedidos.
 */
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

    @NotNull(message = "La sesión es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESSION", nullable = false)
    @JsonIgnoreProperties({"table", "customer", "sessionToken"})
    private QrSession session;

    @NotNull(message = "La presentación es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PRESENTATION", nullable = false)
    @JsonIgnoreProperties({"product", "ingredients", "state", "createdAt"})
    private Presentation presentation;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(1)
    @Max(100)
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @NotNull(message = "El estado del pedido es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false)
    private QrOrderStatus orderStatus = QrOrderStatus.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIRMED_BY")
    @JsonIgnoreProperties({"password", "area", "role", "state", "createdAt"})
    private User confirmedBy;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "CONFIRMED_AT")
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        if (this.orderStatus == null) this.orderStatus = QrOrderStatus.PENDIENTE;
    }

    public enum QrOrderStatus {
        PENDIENTE, CONFIRMADO, CANCELADO
    }
}
