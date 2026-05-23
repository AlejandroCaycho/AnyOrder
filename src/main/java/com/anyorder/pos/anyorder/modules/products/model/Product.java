package com.anyorder.pos.anyorder.modules.products.model;

import com.anyorder.pos.anyorder.modules.categories.model.Category;
import com.anyorder.pos.anyorder.modules.areas.model.Area;
import com.anyorder.pos.anyorder.modules.presentations.model.Presentation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "PRODUCTS", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "ID_CATEGORY", "NAME" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PRODUCT")
    private Integer idProduct;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La categoría es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CATEGORY", nullable = false)
    @JsonIgnoreProperties("products")
    private Category category;

    @NotNull(message = "El área es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_AREA", nullable = false)
    private Area area;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("product")
    private List<Presentation> presentations;

    // Compatibility methods for Service/Repository
    public Integer getIdCategory() {
        return category != null ? category.getIdCategory() : null;
    }

    public void setIdCategory(Integer idCategory) {
        if (this.category == null) this.category = new Category();
        this.category.setIdCategory(idCategory);
    }

    public Integer getIdArea() {
        return area != null ? area.getIdArea() : null;
    }

    public void setIdArea(Integer idArea) {
        if (this.area == null) this.area = new Area();
        this.area.setIdArea(idArea);
    }
}
