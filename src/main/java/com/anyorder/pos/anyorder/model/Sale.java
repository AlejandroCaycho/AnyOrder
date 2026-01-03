package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private Users user;

    @NotNull(message = "El pedido es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ORDER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idOrder")
    @JsonIdentityReference(alwaysAsId = true)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCustomer")
    @JsonIdentityReference(alwaysAsId = true)
    private Customer customer;

    @Column(name = "SALE_DATE", nullable = false, updatable = false)
    private LocalDateTime saleDate;

    @Column(name = "STATE", nullable = false)
    private Integer state = 2; // 0=Anulado, 1=Activo, 2=Completado

    // Trae los detalles de la venta con IDs
    @OneToMany(mappedBy = "sale", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<DetailSale> details;

    // Trae los pagos de la venta con IDs
    @OneToMany(mappedBy = "sale", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<SalePayment> salePayments;

    // Transient para POST
    @Transient
    private List<SalePayment> payments;

    @PrePersist
    protected void onCreate() {
        this.saleDate = LocalDateTime.now();
        if (this.state == null) {
            this.state = 2;
        }
    }
}