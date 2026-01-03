package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

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
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idIngredient")
    @JsonIdentityReference(alwaysAsId = true)
    private Ingredients ingredient;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Column(name = "MOVEMENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "El stock anterior no puede ser negativo")
    @Column(name = "PREVIOUS_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousStock;

    @DecimalMin(value = "0", message = "El nuevo stock no puede ser negativo")
    @Column(name = "NEW_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal newStock;

    @DecimalMin(value = "0", message = "El costo unitario no puede ser negativo")
    @Column(name = "UNIT_COST", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @DecimalMin(value = "0", message = "El costo total no puede ser negativo")
    @Column(name = "TOTAL_COST", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @NotBlank(message = "La razón es obligatoria")
    @Size(min = 3, max = 255, message = "La razón debe tener entre 3 y 255 caracteres")
    @Column(name = "REASON", nullable = false, length = 255)
    private String reason;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private Users user;

    @Column(name = "MOVEMENT_DATE", nullable = false, updatable = false)
    private LocalDateTime movementDate;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.movementDate = LocalDateTime.now();
    }

    public enum MovementType {
        ENTRADA, // Compras, donaciones
        SALIDA, // Consumo en producción
        AJUSTE, // Corrección de inventario
        MERMA, // Pérdidas, vencimientos
        DEVOLUCION // Devoluciones de proveedores
    }
}