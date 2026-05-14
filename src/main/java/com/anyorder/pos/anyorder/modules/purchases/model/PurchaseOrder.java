package com.anyorder.pos.anyorder.modules.purchases.model;

import com.anyorder.pos.anyorder.modules.suppliers.model.Supplier;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SUPPLIER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idSupplier")
    @JsonIdentityReference(alwaysAsId = true)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private User user;

    @Column(name = "ORDER_DATE", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "La fecha de entrega es obligatoria")
    @Column(name = "DELIVERY_DATE", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "RECEIVED_DATE")
    private LocalDateTime receivedDate;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "purchaseOrder", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PurchaseOrderDetail> details;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        if (this.state == null) this.state = true;
        if (this.orderStatus == null) this.orderStatus = OrderStatus.PENDIENTE;
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.PENDIENTE;
    }

    public enum OrderStatus {
        PENDIENTE, CONFIRMADO, EN_TRANSITO, RECIBIDO, CANCELADO
    }

    public enum PaymentStatus {
        PENDIENTE, PAGADO_PARCIAL, PAGADO_COMPLETO
    }
}
