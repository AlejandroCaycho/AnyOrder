package com.anyorder.pos.anyorder.modules.presentations.model;

import com.anyorder.pos.anyorder.modules.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "PRESENTATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Presentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRESENTATION")
    private Integer idPresentation;

    @NotNull(message = "El producto es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCT", nullable = false)
    @JsonIgnoreProperties("presentations")
    private Product product;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "El costo es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "COST", nullable = false, precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "DELIVERY_PRICE", precision = 10, scale = 2)
    private BigDecimal deliveryPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "TAKEOUT_PRICE", precision = 10, scale = 2)
    private BigDecimal takeoutPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "PROMO_PRICE", precision = 10, scale = 2)
    private BigDecimal promoPrice;

    @Min(value = 0)
    @Max(value = 180)
    @Column(name = "PREPARATION_TIME", nullable = false)
    private Integer preparationTime = 0;

    @Size(max = 500)
    @Column(name = "DISH_PHOTO_URL", length = 500)
    private String dishPhotoUrl;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Compatibility methods for Service/Repository
    public Integer getIdProduct() {
        return product != null ? product.getIdProduct() : null;
    }

    public void setIdProduct(Integer idProduct) {
        if (this.product == null) this.product = new Product();
        this.product.setIdProduct(idProduct);
    }
}
