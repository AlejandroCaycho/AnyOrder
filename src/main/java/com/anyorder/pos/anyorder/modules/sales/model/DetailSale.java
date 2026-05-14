package com.anyorder.pos.anyorder.modules.sales.model;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Snapshot de cada ítem vendido.
 * Se crea desde los OrderDetail del pedido al momento de cerrar la venta.
 */
@Entity
@Table(name = "DETAIL_SALE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETAIL")
    private Integer idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SALE", nullable = false)
    @JsonBackReference("sale-details")
    private Sale sale;

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

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
