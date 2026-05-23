package com.anyorder.pos.anyorder.modules.tables.model;

import com.anyorder.pos.anyorder.modules.areas.model.Area;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "TABLES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TABLE")
    private Integer idTable;

    @NotBlank(message = "El nombre de la mesa es obligatorio")
    @Size(min = 2, max = 50)
    @Column(name = "NAME", nullable = false, unique = true)
    private String nameTable;

    @NotNull(message = "El área es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_AREA", nullable = false)
    @JsonIgnoreProperties("tables")
    private Area area;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(1)
    @Max(50)
    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;

    @NotNull
    @Min(0)
    @Column(name = "CURRENT_OCCUPANCY", nullable = false)
    private Integer currentOccupancy = 0;

    @Column(name = "LOCATION", nullable = false)
    private String location = "General";

    @Column(name = "QR_CODE", unique = true)
    private String qrCode;

    @Column(name = "QR_TOKEN", unique = true)
    private String qrToken;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @Column(name = "IS_OCCUPIED", nullable = false)
    private Boolean isOccupied = false;

    @Column(name = "OCCUPIED_SINCE")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occupiedSince;

    @PrePersist
    @PreUpdate
    private void validateOccupancy() {
        if (currentOccupancy != null && capacity != null &&
                currentOccupancy > capacity) {
            throw new IllegalArgumentException(
                    "La ocupación actual no puede exceder la capacidad");
        }
    }


    // Compatibility methods for Service/Repository
    public String getName() {
        return nameTable;
    }

    public void setName(String name) {
        this.nameTable = name;
    }

    public Integer getIdArea() {
        return area != null ? area.getIdArea() : null;
    }

    public void setIdArea(Integer idArea) {
        if (this.area == null) this.area = new Area();
        this.area.setIdArea(idArea);
    }
}
