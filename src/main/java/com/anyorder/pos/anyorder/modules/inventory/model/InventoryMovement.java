package com.anyorder.pos.anyorder.modules.inventory.model;

import com.anyorder.pos.anyorder.modules.ingredients.model.Ingredient;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "INVENTORY_MOVEMENTS")
@Getter
@Setter
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
    @JsonIgnoreProperties({"supplier", "state", "createdAt", "expirationDate", "description", "unitPrice", "minStock", "category"})
    private Ingredient ingredient;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "MOVEMENT_TYPE", nullable = false)
    private MovementType movementType;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "PREVIOUS_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal previousStock;

    @Column(name = "NEW_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal newStock;

    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_COST", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @DecimalMin(value = "0.0")
    @Column(name = "TOTAL_COST", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 3, message = "El motivo debe tener al menos 3 caracteres")
    @Column(name = "REASON", nullable = false)
    private String reason;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIgnoreProperties({"password", "area", "role", "state", "createdAt", "email", "phone", "documentType", "documentNumber"})
    private User user;

    @CreationTimestamp
    @Column(name = "MOVEMENT_DATE", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime movementDate;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    public enum MovementType {
        ENTRADA, SALIDA, AJUSTE, MERMA, DEVOLUCION
    }
    
    // Compatibility methods for Service/Repository (if needed)
    public Integer getIdIngredient() {
        return ingredient != null ? ingredient.getIdIngredient() : null;
    }
    
    public void setIdIngredient(Integer idIngredient) {
        if (this.ingredient == null) this.ingredient = new Ingredient();
        this.ingredient.setIdIngredient(idIngredient);
    }
    
    public Integer getIdUser() {
        return user != null ? user.getIdUser() : null;
    }
    
    public void setIdUser(Integer idUser) {
        if (this.user == null) this.user = new User();
        this.user.setIdUser(idUser);
    }
}
