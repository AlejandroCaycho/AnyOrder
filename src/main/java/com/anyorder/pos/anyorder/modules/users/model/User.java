package com.anyorder.pos.anyorder.modules.users.model;

import com.anyorder.pos.anyorder.modules.areas.model.Area;
import com.anyorder.pos.anyorder.modules.roles.model.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_USER")
    private Integer idUser;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100)
    @Column(name = "SURNAMES", nullable = false, length = 100)
    private String surnames;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 20)
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", length = 1)
    private Gender gender;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 20)
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    @Column(name = "ADRESS", nullable = false, length = 255)
    private String adress;

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
    private Role role;

    @NotNull(message = "El área es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_AREA", nullable = false)
    private Area area;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(name = "HORA_INICIO", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(name = "HORA_FIN", nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    @NotNull(message = "Las horas planificadas son obligatorias")
    @DecimalMin(value = "0.1")
    @DecimalMax(value = "24.0")
    @Column(name = "PLANNED_HOURS", nullable = false, precision = 4, scale = 2)
    private BigDecimal plannedHours;

    @NotBlank(message = "El turno es obligatorio")
    @Column(name = "TURNO", nullable = false, length = 50)
    private String turno;

    @NotNull(message = "La tarifa por hora es obligatoria")
    @DecimalMin(value = "0.0")
    @Column(name = "HOURLY_RATE", nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    public enum DocumentType {
        DNI, CEX, PAS, RUC
    }

    public enum Gender {
        M, F, O
    }
}
