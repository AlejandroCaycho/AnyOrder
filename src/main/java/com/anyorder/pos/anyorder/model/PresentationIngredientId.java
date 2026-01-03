package com.anyorder.pos.anyorder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresentationIngredientId implements Serializable {
    private Integer idPresentation;
    private Integer idIngredient;
}