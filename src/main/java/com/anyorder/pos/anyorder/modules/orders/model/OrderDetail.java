package com.anyorder.pos.anyorder.modules.orders.model;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;

@Entity
@Table(name = "ORDER_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETAIL")
    private Integer idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ORDER", nullable = false)
    @JsonBackReference("order-details")
    private Order order;

    @NotNull(message = "La presentación es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PRESENTATION", nullable = false)
    private Presentation presentation;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(1)
    @Max(100)
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;
}
