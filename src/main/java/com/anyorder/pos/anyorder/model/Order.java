package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;
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
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private Users user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCustomer")
    @JsonIdentityReference(alwaysAsId = true)
    private Customer customer;

    @Column(name = "ORDER_DATE", nullable = false, updatable = false)
    private LocalDateTime orderDate;

    @Column(name = "ORDER_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @NotNull(message = "El tipo de consumo es obligatorio")
    @Column(name = "TYPE_OF_CONSUMPTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsumptionType consumptionType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TABLE")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idTable")
    @JsonIdentityReference(alwaysAsId = true)
    private Tables table;

    @DecimalMin(value = "0", message = "El total debe ser mayor o igual a 0")
    @Column(name = "TOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "TOTAL_ITEMS", nullable = false)
    private Integer totalItems = 0;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Max(value = 50, message = "No puede haber más de 50 personas")
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople = 1;

    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    @Column(name = "DELIVERY_ADDRESS", length = 255)
    private String deliveryAddress;

    @Column(name = "CUSTOMER_NOTES", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "CONFIRMED", nullable = false)
    private Boolean confirmed = false;

    // Trae los detalles pero solo con IDs
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderDetail> details;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.PENDIENTE;
        }
        if (this.confirmed == null) {
            this.confirmed = false;
        }
    }

    public enum OrderStatus {
        PENDIENTE, EN_PREPARACION, LISTO, ENTREGADO, CERRADO, CANCELADO
    }

    public enum ConsumptionType {
        LOCAL, DELIVERY, TAKEOUT, PROMO
    }
}