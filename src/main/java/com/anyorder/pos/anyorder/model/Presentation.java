package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRESENTATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESENTATION")
    private Integer idPresentation;

    @NotNull(message = "El producto es obligatorio")
    @Column(name = "ID_PRODUCT", nullable = false)
    private Integer idProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCT", insertable = false, updatable = false)
    @JsonBackReference("product-presentations") // ✅ CAMBIO
    private Product product;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El costo debe ser mayor o igual a 0")
    @Column(name = "COST", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de delivery debe ser mayor o igual a 0")
    @Column(name = "DELIVERY_PRICE", precision = 10, scale = 2)
    private BigDecimal deliveryPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio para llevar debe ser mayor o igual a 0")
    @Column(name = "TAKEOUT_PRICE", precision = 10, scale = 2)
    private BigDecimal takeoutPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio promocional debe ser mayor o igual a 0")
    @Column(name = "PROMO_PRICE", precision = 10, scale = 2)
    private BigDecimal promoPrice;

    @Min(value = 0, message = "El tiempo de preparación debe ser mayor o igual a 0")
    @Max(value = 180, message = "El tiempo de preparación no puede exceder 180 minutos")
    @Column(name = "PREPARATION_TIME", nullable = false)
    private Integer preparationTime = 0;

    @Size(max = 500, message = "La URL de la foto no puede exceder 500 caracteres")
    @Column(name = "DISH_PHOTO_URL", length = 500)
    private String dishPhotoUrl;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}