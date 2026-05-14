package com.anyorder.pos.anyorder.modules.purchases.model;

import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Línea de detalle de una orden de compra.
 * El subtotal se calcula automáticamente en el servicio.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PURCHASE_ORDER", nullable = false)
    @JsonBackReference("po-details")
    private PurchaseOrder purchaseOrder;

    @NotNull(message = "El ingrediente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INGREDIENT", nullable = false)
    @JsonIgnoreProperties({"supplier", "state", "createdAt", "expirationDate"})
    private Ingredient ingredient;

    @NotNull(message = "La cantidad pedida es obligatoria")
    @DecimalMin(value = "0.01")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0")
    @Column(name = "SUBTOTAL", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Cantidad realmente recibida (se actualiza al marcar RECIBIDO) */
    @DecimalMin(value = "0.0")
    @Column(name = "RECEIVED_QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;
}
