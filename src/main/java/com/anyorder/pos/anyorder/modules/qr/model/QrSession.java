package com.anyorder.pos.anyorder.modules.qr.model;

import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Representa una sesión activa de un cliente en una mesa mediante QR.
 */
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
    @JsonIgnoreProperties({"area", "occupiedSince", "currentOccupancy"})
    private Tables table;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CUSTOMER")
    @JsonIgnoreProperties({"role", "state", "createdAt"})
    private Customer customer;

    @NotBlank(message = "El token de sesión es obligatorio")
    @Size(max = 100)
    @Column(name = "SESSION_TOKEN", nullable = false, unique = true, length = 100)
    private String sessionToken;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100)
    @Column(name = "CUSTOMER_NAME", length = 100)
    private String customerName;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(1)
    @Max(50)
    @Column(name = "NUMBER_OF_PEOPLE", nullable = false)
    private Integer numberOfPeople = 1;

    @NotNull(message = "El estado de la sesión es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "SESSION_STATUS", nullable = false)
    private SessionStatus sessionStatus = SessionStatus.ACTIVA;

    @CreationTimestamp
    @Column(name = "STARTED_AT", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @Column(name = "EXPIRES_AT")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (this.sessionStatus == null) this.sessionStatus = SessionStatus.ACTIVA;
        if (this.expiresAt == null) this.expiresAt = LocalDateTime.now().plusHours(4); // Default 4 hours
    }

    public enum SessionStatus {
        ACTIVA, CERRADA, EXPIRADA
    }
}
