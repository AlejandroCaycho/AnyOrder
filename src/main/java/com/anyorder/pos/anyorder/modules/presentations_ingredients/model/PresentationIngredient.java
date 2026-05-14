package com.anyorder.pos.anyorder.modules.presentations_ingredients.model;

import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;

@Entity
@Table(name = "PRESENTATION_INGREDIENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresentationIngredient {

    @EmbeddedId
    private PresentationIngredientId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPresentation")
    @JoinColumn(name = "ID_PRESENTATION")
    @JsonBackReference("presentation-ingredients")
    private Presentation presentation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idIngredient")
    @JoinColumn(name = "ID_INGREDIENT")
    @JsonBackReference("ingredient-presentations")
    private Ingredient ingredient;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotBlank(message = "La unidad es obligatoria")
    @Column(name = "UNIT", nullable = false, length = 20)
    private String unit;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;
}
