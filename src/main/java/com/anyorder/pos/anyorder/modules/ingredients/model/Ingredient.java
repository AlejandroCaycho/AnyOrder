package com.anyorder.pos.anyorder.modules.ingredients.model;

import com.anyorder.pos.anyorder.modules.suppliers.model.Supplier;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "INGREDIENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INGREDIENT")
    private Integer idIngredient;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 2, max = 50)
    @Column(name = "CODE", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 2, max = 50)
    @Column(name = "CATEGORY", nullable = false, length = 50)
    private String category;

    @NotBlank(message = "La unidad es obligatoria")
    @Size(min = 1, max = 20)
    @Column(name = "UNIT", nullable = false, length = 20)
    private String unit;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.0")
    @Column(name = "QUANTITY", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity = BigDecimal.ZERO;

    @NotNull(message = "El stock mínimo es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "MIN_STOCK", nullable = false, precision = 10, scale = 2)
    private BigDecimal minStock = BigDecimal.ZERO;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(value = "0.0")
    @Column(name = "UNIT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SUPPLIER", nullable = false)
    @JsonIgnoreProperties("ingredients")
    private Supplier supplier;

    // Compatibility methods for Service/Repository
    public Integer getIdSupplier() {
        return supplier != null ? supplier.getIdSupplier() : null;
    }

    public void setIdSupplier(Integer idSupplier) {
        if (this.supplier == null) this.supplier = new Supplier();
        this.supplier.setIdSupplier(idSupplier);
    }

    @Column(name = "EXPIRATION_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expirationDate;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
