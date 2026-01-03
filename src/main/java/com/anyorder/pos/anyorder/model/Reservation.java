package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RESERVATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESERVATION")
    private Integer idReservation;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCustomer")
    @JsonIdentityReference(alwaysAsId = true)
    private Customer customer;

    @NotNull(message = "La mesa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TABLE", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idTable")
    @JsonIdentityReference(alwaysAsId = true)
    private Tables table;

    @NotNull(message = "La fecha de reservación es obligatoria")
    @Column(name = "RESERVATION_DATE", nullable = false)
    private LocalDateTime reservationDate;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Max(value = 50, message = "El número de personas no puede exceder 50")
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople;

    // Estos campos se llenan automáticamente desde Customer
    @Column(name = "CUSTOMER_NAME", nullable = false, length = 200)
    private String customerName;

    @Column(name = "CUSTOMER_PHONE", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "SPECIAL_REQUESTS", columnDefinition = "TEXT")
    private String specialRequests;

    @NotNull(message = "El estado de la reservación es obligatorio")
    @Column(name = "RESERVATION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus = ReservationStatus.PENDIENTE;

    @NotNull(message = "El usuario creador es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CREATED_BY", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private Users createdBy;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.reservationStatus == null) {
            this.reservationStatus = ReservationStatus.PENDIENTE;
        }
        validateReservationDate();
    }

    @PreUpdate
    protected void onUpdate() {
        validateReservationDate();
    }

    private void validateReservationDate() {
        if (this.reservationDate != null && this.createdAt != null) {
            if (this.reservationDate.isBefore(this.createdAt)) {
                throw new IllegalArgumentException(
                        "La fecha de reservación no puede ser anterior a la fecha de creación");
            }
        }
    }

    public enum ReservationStatus {
        PENDIENTE,
        CONFIRMADA,
        CANCELADA,
        COMPLETADA,
        NO_SHOW
    }
}