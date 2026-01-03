package com.anyorder.pos.anyorder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Integer idUser;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    @Column(name = "SURNAMES", nullable = false, length = 100)
    private String surnames;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @JsonIgnore // CRÍTICO: NUNCA exponer el password en JSON
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 20, message = "El documento debe tener entre 8 y 20 caracteres")
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER")
    private Gender gender;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 20, message = "El teléfono debe tener entre 9 y 20 caracteres")
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "ADRESS", nullable = false, length = 255)
    private String address;

    @Column(name = "PROFILE_PHOTO", length = 255)
    private String profilePhoto;

    @CreationTimestamp
    @Column(name = "REGISTRATION_DATE", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idRole")
    @JsonIdentityReference(alwaysAsId = true)
    private Role role;

    @NotNull(message = "El área es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_AREA", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idArea")
    @JsonIdentityReference(alwaysAsId = true)
    private Area area;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "HORA_INICIO", nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "HORA_FIN", nullable = false)
    private LocalTime horaFin;

    @NotNull(message = "Las horas planificadas son obligatorias")
    @DecimalMin(value = "0.01", message = "Las horas planificadas deben ser mayores a 0")
    @DecimalMax(value = "24.00", message = "Las horas planificadas no pueden exceder 24")
    @Column(name = "PLANNED_HOURS", nullable = false, precision = 4, scale = 2)
    private BigDecimal plannedHours;

    @NotBlank(message = "El turno es obligatorio")
    @Size(min = 3, max = 50, message = "El turno debe tener entre 3 y 50 caracteres")
    @Column(name = "TURNO", nullable = false, length = 50)
    private String turno;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.00", message = "La tarifa debe ser mayor o igual a 0")
    @Column(name = "HOURLY_RATE", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    // Enums
    public enum DocumentType {
        DNI, CEX, PAS, RUC
    }

    public enum Gender {
        M, F, O
    }
}