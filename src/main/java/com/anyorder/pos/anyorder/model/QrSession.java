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
@Table(name = "QR_SESSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SESSION")
    private Integer idSession;

    @NotNull(message = "La mesa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TABLE", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idTable")
    @JsonIdentityReference(alwaysAsId = true)
    private Tables table;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idCustomer")
    @JsonIdentityReference(alwaysAsId = true)
    private Customer customer;

    @Size(min = 10, max = 100, message = "El token debe tener entre 10 y 100 caracteres")
    @Column(name = "SESSION_TOKEN", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @Size(max = 100, message = "El nombre del cliente no puede exceder 100 caracteres")
    @Column(name = "CUSTOMER_NAME", length = 100)
    private String customerName;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Max(value = 50, message = "El número de personas no puede exceder 50")
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople = 1;

    @NotNull(message = "El estado de la sesión es obligatorio")
    @Column(name = "SESSION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus = SessionStatus.ACTIVA;

    @Column(name = "STARTED_AT", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        this.startedAt = LocalDateTime.now();
        if (this.sessionStatus == null) {
            this.sessionStatus = SessionStatus.ACTIVA;
        }
        if (this.numberOfPeople == null) {
            this.numberOfPeople = 1;
        }
        validateClosedDate();
    }

    @PreUpdate
    protected void onUpdate() {
        validateClosedDate();
    }

    private void validateClosedDate() {
        if (this.closedAt != null && this.startedAt != null) {
            if (this.closedAt.isBefore(this.startedAt)) {
                throw new IllegalArgumentException(
                        "La fecha de cierre no puede ser anterior a la fecha de inicio");
            }
        }
    }

    public enum SessionStatus {
        ACTIVA,
        CERRADA,
        EXPIRADA
    }
}