package com.anyorder.pos.anyorder.modules.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "USER_LOGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_LOG")
    private Integer idLog;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_USER", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idUser")
    @JsonIdentityReference(alwaysAsId = true)
    private User user;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "FECHA", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;

    @NotNull(message = "La hora de entrada es obligatoria")
    @Column(name = "HORA_ENTRADA", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime horaEntrada;

    @Column(name = "HORA_SALIDA")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime horaSalida;

    @DecimalMin(value = "0.0", message = "Las horas trabajadas deben ser >= 0")
    @DecimalMax(value = "24.0", message = "Las horas trabajadas no pueden exceder 24")
    @Column(name = "HOURS_WORKED", precision = 4, scale = 2)
    private BigDecimal hoursWorked;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.0", message = "La tarifa debe ser mayor o igual a 0")
    @Column(name = "HOURLY_RATE", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @NotNull(message = "Las horas planificadas son obligatorias")
    @DecimalMin(value = "0.01", message = "Las horas planificadas deben ser mayor a 0")
    @DecimalMax(value = "24.0", message = "Las horas planificadas no pueden exceder 24")
    @Column(name = "PLANNED_HOURS", nullable = false, precision = 4, scale = 2)
    private BigDecimal plannedHours;

    @NotNull(message = "El estado es obligatorio")
    @Column(name = "STATE", nullable = false)
    private Integer state = 1; // 0=Inactivo, 1=Activo, 2=Cerrado

    @PrePersist
    protected void onCreate() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.horaEntrada == null) {
            this.horaEntrada = LocalDateTime.now();
        }
        if (this.state == null) {
            this.state = 1;
        }
    }

    public void registrarSalida() {
        this.horaSalida = LocalDateTime.now();
        calcularHorasTrabajadas();
        this.state = 2; // Cerrado
    }

    public void calcularHorasTrabajadas() {
        if (this.horaEntrada != null && this.horaSalida != null) {
            Duration duration = Duration.between(this.horaEntrada, this.horaSalida);
            double hours = duration.toMinutes() / 60.0;
            this.hoursWorked = BigDecimal.valueOf(hours).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }

    public BigDecimal calcularPago() {
        if (this.hoursWorked != null && this.hourlyRate != null) {
            return this.hoursWorked.multiply(this.hourlyRate);
        }
        return BigDecimal.ZERO;
    }
}
