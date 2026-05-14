package com.anyorder.pos.anyorder.modules.purchases.model;

import com.anyorder.pos.anyorder.modules.suppliers.model.Supplier;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orden de compra a proveedor.
 *
 * Flujo de estados:
 *   PENDIENTE → CONFIRMADO → EN_TRANSITO → RECIBIDO
 *                          ↘ CANCELADO (cualquier estado antes de RECIBIDO)
 *
 * Al marcar RECIBIDO se actualizan los stocks de ingredientes (ENTRADA).
 */
@Entity
@Table(name = "PURCHASE_ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PURCHASE_ORDER")
    private Integer idPurchaseOrder;

    @NotBlank(message = "El número de orden es obligatorio")
    @Size(min = 3, max = 50)
    @Column(name = "ORDER_NUMBER", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SUPPLIER", nullable = false)
    @JsonIgnoreProperties({"notes", "state", "createdAt"})
    private Supplier supplier;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIgnoreProperties({"password", "area", "role"})
    private User user;

    @Column(name = "ORDER_DATE", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "La fecha de entrega estimada es obligatoria")
    @Column(name = "DELIVERY_DATE", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "RECEIVED_DATE")
    private LocalDateTime receivedDate;

    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDIENTE;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("po-details")
    private List<PurchaseOrderDetail> details;

    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) this.orderDate = LocalDateTime.now();
        if (this.orderStatus == null) this.orderStatus = OrderStatus.PENDIENTE;
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.PENDIENTE;
        if (this.state == null) this.state = true;
    }

    public enum OrderStatus {
        PENDIENTE, CONFIRMADO, EN_TRANSITO, RECIBIDO, CANCELADO
    }

    public enum PaymentStatus {
        PENDIENTE, PAGADO_PARCIAL, PAGADO_COMPLETO
    }
}
