package com.anyorder.pos.anyorder.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SUPPLIERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SUPPLIER")
    private Integer idSupplier;

    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "El nombre del contacto es obligatorio")
    @Size(min = 3, max = 100, message = "El contacto debe tener entre 3 y 100 caracteres")
    @Column(name = "CONTACT_NAME", nullable = false, length = 100)
    private String contactName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, message = "El teléfono debe tener al menos 9 dígitos")
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Email(message = "Email debe ser válido")
    @Column(name = "EMAIL", length = 150)
    private String email;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, message = "El documento debe tener al menos 8 caracteres")
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "PAYMENT_TERMS", length = 100)
    private String paymentTerms;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "STATE", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean state = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DocumentType {
        RUC, DNI, CEX
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.state == null) {
            this.state = true;
        }
    }
}