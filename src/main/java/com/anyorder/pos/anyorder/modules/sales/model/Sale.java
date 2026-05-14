package com.anyorder.pos.anyorder.modules.sales.model;

import com.anyorder.pos.anyorder.modules.users.model.User;
import com.anyorder.pos.anyorder.modules.orders.model.Order;
import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa una venta cerrada.
 *
 * Flujo:
 *   Orden (PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO)
 *   → Sale se crea cuando el pedido llega a ENTREGADO
 *   → Sale registra los detalles y los pagos
 *   → Orden pasa a CERRADO
 *
 * States:
 *   0 = ANULADA
 *   1 = VIGENTE
 *   2 = CERRADA (default)
 */
@Entity
@Table(name = "SALES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SALE")
    private Integer idSale;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIgnoreProperties({"password", "area", "role"})
    private User user;

    @NotNull(message = "El pedido es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ORDER", nullable = false)
    @JsonIgnoreProperties({"details", "user"})
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    @JsonIgnoreProperties({"role"})
    private Customer customer;

    @CreationTimestamp
    @Column(name = "SALE_DATE", nullable = false, updatable = false)
    private LocalDateTime saleDate;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "STATE", nullable = false)
    private Integer state = 1; // 0=ANULADA, 1=VIGENTE

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("sale-details")
    private List<DetailSale> details;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("sale-payments")
    private List<SalePayment> payments;

    @Transient
    public String getStateDescription() {
        return switch (state != null ? state : -1) {
            case 0 -> "ANULADA";
            case 1 -> "VIGENTE";
            default -> "DESCONOCIDO";
        };
    }
}
