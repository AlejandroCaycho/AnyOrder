package com.anyorder.pos.anyorder.modules.suppliers.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 150)
    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Size(min = 3, max = 100)
    @Column(name = "CONTACT_NAME", nullable = false, length = 100)
    private String contactName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 20)
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Email(message = "Formato de email inválido")
    @Column(name = "EMAIL", length = 150)
    private String email;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 8, max = 20)
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "PAYMENT_TERMS", length = 100)
    private String paymentTerms;

    @Column(name = "NOTES", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum DocumentType {
        RUC, DNI, CEX
    }
}
