package com.anyorder.pos.anyorder.modules.reservations.model;

import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Representa una reservación de mesa por un cliente.
 */
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
    @JsonIgnoreProperties({"role", "state", "createdAt"})
    private Customer customer;

    @NotNull(message = "La mesa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TABLE", nullable = false)
    @JsonIgnoreProperties({"area", "occupiedSince", "currentOccupancy"})
    private Tables table;

    @NotNull(message = "La fecha de reservación es obligatoria")
    @Column(name = "RESERVATION_DATE", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationDate;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1)
    @Max(value = 50)
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 200)
    @Column(name = "CUSTOMER_NAME", nullable = false, length = 200)
    private String customerName;

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Size(max = 20)
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
    @JsonIgnoreProperties({"password", "area", "role", "state", "createdAt"})
    private User createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (this.reservationStatus == null) {
            this.reservationStatus = ReservationStatus.PENDIENTE;
        }
    }

    public enum ReservationStatus {
        PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA, NO_SHOW
    }
}
