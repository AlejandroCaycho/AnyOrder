package com.anyorder.pos.anyorder.modules.orders.model;

import com.anyorder.pos.anyorder.modules.users.model.User;
import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ORDER")
    private Integer idOrder;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    private Customer customer;

    @CreationTimestamp
    @Column(name = "ORDER_DATE", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "ORDER_STATUS", nullable = false)
    private OrderStatus orderStatus;

    @NotNull(message = "El tipo de consumo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_OF_CONSUMPTION", nullable = false)
    private ConsumptionType typeOfConsumption;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TABLE")
    private Tables table;

    @NotNull(message = "El total es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El total de ítems es obligatorio")
    @Min(0)
    @Max(500)
    @Column(name = "TOTAL_ITEMS", nullable = false)
    private Integer totalItems = 0;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(1)
    @Max(50)
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople = 1;

    @Column(name = "DELIVERY_ADDRESS", length = 255)
    private String deliveryAddress;

    @Column(name = "CUSTOMER_NOTES", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed = false;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-details")
    private List<OrderDetail> details;

    public enum OrderStatus {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CERRADO, CANCELADO
    }

    public enum ConsumptionType {
        LOCAL, DELIVERY, TAKEOUT, PROMO
    }
}
