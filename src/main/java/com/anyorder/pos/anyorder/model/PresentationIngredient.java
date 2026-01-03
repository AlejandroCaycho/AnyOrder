package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "PRESENTATION_INGREDIENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PresentationIngredientId.class)
public class PresentationIngredient {

    @Id
    @Column(name = "ID_PRESENTATION")
    private Integer idPresentation;

    @Id
    @Column(name = "ID_INGREDIENT")
    private Integer idIngredient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PRESENTATION", insertable = false, updatable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idPresentation")
    @JsonIdentityReference(alwaysAsId = true)
    private Presentation presentation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INGREDIENT", insertable = false, updatable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idIngredient")
    @JsonIdentityReference(alwaysAsId = true)
    private Ingredients ingredient;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @DecimalMax(value = "10000.0", message = "La cantidad no puede exceder 10000")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotBlank(message = "La unidad es obligatoria")
    @Size(min = 1, max = 20, message = "La unidad debe tener entre 1 y 20 caracteres")
    @Column(name = "UNIT", nullable = false, length = 20)
    private String unit;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    /**
     * Calcula el costo del ingrediente para esta presentación
     */
    public BigDecimal calculateIngredientCost() {
        if (this.ingredient != null && this.ingredient.getUnitPrice() != null && this.quantity != null) {
            return this.ingredient.getUnitPrice().multiply(this.quantity);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Verifica si hay stock suficiente del ingrediente
     */
    public boolean hasEnoughStock() {
        if (this.ingredient == null || this.ingredient.getQuantity() == null) {
            return false;
        }
        return this.ingredient.getQuantity().compareTo(this.quantity) >= 0;
    }
}