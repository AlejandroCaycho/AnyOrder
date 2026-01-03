package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PRODUCTS", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "ID_CATEGORY", "NAME" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRODUCT")
    private Integer idProduct;

    @NotNull(message = "La categoría es obligatoria")
    @Column(name = "ID_CATEGORY", nullable = false)
    private Integer idCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CATEGORY", insertable = false, updatable = false)
    @JsonBackReference("category-products") // ✅ CAMBIO
    private Category category;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "El área es obligatoria")
    @Column(name = "ID_AREA", nullable = false)
    private Integer idArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AREA", insertable = false, updatable = false)
    @JsonBackReference("area-products") // ✅ CAMBIO
    private Area area;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PRODUCT")
    @JsonManagedReference("product-presentations") // ✅ CAMBIO
    private List<Presentation> presentations;
}