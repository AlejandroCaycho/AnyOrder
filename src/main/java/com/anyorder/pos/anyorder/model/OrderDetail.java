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
@Table(name = "ORDER_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETAIL")
    private Integer idDetail;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ORDER", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PRESENTATION", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idPresentation")
    @JsonIdentityReference(alwaysAsId = true)
    private Presentation presentation;

    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Max(value = 100, message = "La cantidad no puede exceder 100")
    @Column(name = "AMOUNT", nullable = false)
    private Integer amount;

    @DecimalMin(value = "0", message = "El precio unitario debe ser mayor o igual a 0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0", message = "El subtotal debe ser mayor o igual a 0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Size(max = 255, message = "Las notas no deben exceder 255 caracteres")
    @Column(name = "NOTES", length = 255)
    private String notes;

    /**
     * Calcula el subtotal: unitPrice * amount
     */
    public void calculateSubtotal() {
        if (this.unitPrice != null && this.amount != null) {
            this.subtotal = this.unitPrice.multiply(new BigDecimal(this.amount));
        }
    }
}