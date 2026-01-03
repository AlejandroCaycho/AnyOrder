package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    // Evita recursión: Sale -> DetailSale -> Sale
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SALE", nullable = false)
    @JsonIgnore
    private Sale sale;

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

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor o igual a 0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal debe ser mayor o igual a 0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Calcula el subtotal: unitPrice * amount
     */
    public void calculateSubtotal() {
        if (this.unitPrice != null && this.amount != null) {
            this.subtotal = this.unitPrice.multiply(new BigDecimal(this.amount));
        }
    }
}