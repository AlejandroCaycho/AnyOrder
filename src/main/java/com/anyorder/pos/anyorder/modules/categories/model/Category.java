package com.anyorder.pos.anyorder.modules.categories.model;

import com.anyorder.pos.anyorder.modules.products.model.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "CATEGORIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CATEGORY")
    private Integer idCategory;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "La sección es obligatoria")
    @Size(min = 2, max = 100, message = "La sección debe tener entre 2 y 100 caracteres")
    @Column(name = "SECTION", nullable = false, length = 100)
    private String section;

    @Column(name = "DELIVERY", nullable = false)
    private Boolean delivery = false;

    @Min(value = 0, message = "El orden de visualización debe ser mayor o igual a 0")
    @Column(name = "DISPLAY_ORDER", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonManagedReference("category-products")
    private List<Product> products;
}
