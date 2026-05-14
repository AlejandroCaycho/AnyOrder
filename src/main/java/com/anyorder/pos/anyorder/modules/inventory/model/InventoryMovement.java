package com.anyorder.pos.anyorder.modules.inventory.model;

import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY_MOVEMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_MOVEMENT")
    private Integer idMovement;

    @NotNull(message = "El ingrediente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_INGREDIENT", nullable = false)
    @JsonIgnoreProperties({"supplier", "state", "createdAt", "expirationDate"})
    private Ingredient ingredient;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "MOVEMENT_TYPE", nullable = false)
    private MovementType movementType;

    @NotNull(message = "La cantidad es obligatoria")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @NotNull(message = "El stock anterior es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "PREVIOUS_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousStock;

    @NotNull(message = "El stock nuevo es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "NEW_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal newStock;

    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_COST", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL_COST", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 3)
    @Column(name = "REASON", nullable = false)
    private String reason;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIgnoreProperties({"password", "area", "role"})
    private User user;

    @CreationTimestamp
    @Column(name = "MOVEMENT_DATE", nullable = false, updatable = false)
    private LocalDateTime movementDate;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    public enum MovementType {
        ENTRADA, SALIDA, AJUSTE, MERMA, DEVOLUCION
    }
}
