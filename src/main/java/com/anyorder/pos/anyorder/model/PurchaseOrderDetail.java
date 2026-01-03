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
@Table(name = "PURCHASE_ORDER_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DETAIL")
    private Integer idDetail;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PURCHASE_ORDER", nullable = false)
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "El ingrediente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INGREDIENT", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idIngredient")
    @JsonIdentityReference(alwaysAsId = true)
    private Ingredients ingredient;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @DecimalMax(value = "100000.0", message = "La cantidad no puede exceder 100000")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio debe ser mayor o igual a 0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "El subtotal es obligatorio")
    @DecimalMin(value = "0.0", message = "El subtotal debe ser mayor o igual a 0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0", message = "La cantidad recibida no puede ser negativa")
    @Column(name = "RECEIVED_QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    /**
     * Calcula el subtotal: quantity * unitPrice
     */
    public void calculateSubtotal() {
        if (this.quantity != null && this.unitPrice != null) {
            this.subtotal = this.quantity.multiply(this.unitPrice);
        }
    }

    /**
     * Verifica si el detalle está completamente recibido
     */
    public boolean isFullyReceived() {
        if (this.receivedQuantity == null || this.quantity == null) {
            return false;
        }
        return this.receivedQuantity.compareTo(this.quantity) >= 0;
    }
}