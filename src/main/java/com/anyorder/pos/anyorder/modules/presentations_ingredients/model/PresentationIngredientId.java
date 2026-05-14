package com.anyorder.pos.anyorder.modules.presentations_ingredients.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresentationIngredientId implements Serializable {

    @Column(name = "ID_PRESENTATION")
    private Integer idPresentation;

    @Column(name = "ID_INGREDIENT")
    private Integer idIngredient;
}
