package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@Entity
@Table(name = "TABLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TABLE")
    private Integer idTable;

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    // =========================
    // RELACIÓN CON AREA
    // =========================
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ID_AREA", nullable = false)
    private Area area;

    // Permite JSON plano sin DTO
    @JsonProperty("idArea")
    public void setIdArea(Integer idArea) {
        if (idArea != null) {
            this.area = new Area();
            this.area.setIdArea(idArea);
        }
    }

    @JsonProperty("idArea")
    public Integer getIdArea() {
        return area != null ? area.getIdArea() : null;
    }

    // =========================
    // CAMPOS TABLA
    // =========================
    @NotNull
    @Min(1)
    @Max(50)
    @Column(name = "CAPACITY", nullable = false)
    private Integer capacity;

    @NotNull
    @Min(0)
    @Column(name = "CURRENT_OCCUPANCY", nullable = false)
    private Integer currentOccupancy = 0;

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(name = "LOCATION", nullable = false)
    private String location;

    @Column(name = "QR_CODE", unique = true)
    private String qrCode;

    @Column(name = "QR_TOKEN", unique = true)
    private String qrToken;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @Column(name = "IS_OCCUPIED", nullable = false)
    private Boolean isOccupied = false;

    @Column(name = "OCCUPIED_SINCE")
    private LocalDateTime occupiedSince;

    // =========================
    // VALIDACIONES
    // =========================
    @PrePersist
    @PreUpdate
    private void validateOccupancy() {
        if (currentOccupancy != null && capacity != null &&
                currentOccupancy > capacity) {
            throw new IllegalArgumentException(
                    "La ocupación actual no puede exceder la capacidad");
        }
    }
}
