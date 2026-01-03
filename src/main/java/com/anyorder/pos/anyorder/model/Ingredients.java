package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "INGREDIENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ingredients {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INGREDIENT")
    private Integer idIngredient;

    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El código del ingrediente es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 50, message = "La categoría debe tener entre 2 y 50 caracteres")
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Size(min = 1, max = 20, message = "La unidad debe tener entre 1 y 20 caracteres")
    @Column(name = "UNIT", nullable = false, length = 20)
    private String unit;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.00", message = "La cantidad no puede ser negativa")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0.00", message = "El stock mínimo no puede ser negativo")
    @Column(name = "MIN_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal minStock = BigDecimal.ZERO;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SUPPLIER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idSupplier")
    @JsonIdentityReference(alwaysAsId = true)
    private Supplier supplier;

    @Column(name = "EXPIRATION_DATE")
    private LocalDate expirationDate;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "STATE", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean state = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = true;
        }
        if (this.quantity == null) {
            this.quantity = BigDecimal.ZERO;
        }
        if (this.minStock == null) {
            this.minStock = BigDecimal.ZERO;
        }
        if (this.unitPrice == null) {
            this.unitPrice = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si el stock está bajo el mínimo
     */
    public boolean isLowStock() {
        return this.quantity.compareTo(this.minStock) <= 0;
    }

    /**
     * Verifica si el ingrediente está vencido
     */
    public boolean isExpired() {
        if (this.expirationDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(this.expirationDate);
    }

    /**
     * Calcula el costo total del inventario
     */
    public BigDecimal getTotalCost() {
        if (this.quantity == null || this.unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return this.quantity.multiply(this.unitPrice);
    }
}