package com.anyorder.pos.anyorder.modules.customers.model;

import com.anyorder.pos.anyorder.modules.roles.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "CUSTOMERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CUSTOMER")
    private Integer idCustomer;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Email(message = "Formato de email inválido")
    @Column(name = "EMAIL", length = 150)
    private String email;

    @JsonIgnore
    @Column(name = "PASSWORD", length = 255)
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @NotNull(message = "El tipo de documento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    private DocumentType documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_TYPE", nullable = false)
    private CustomerType customerType = CustomerType.NATURAL;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROLE")
    private Role role;

    @Column(name = "LAST_PURCHASE_DATE")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPurchaseDate;

    @Column(name = "TOTAL_PURCHASES", nullable = false)
    private Integer totalPurchases = 0;

    @Column(name = "TOTAL_SPENT", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @Column(name = "CREATED_AT")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum DocumentType {
        DNI, CEX, PAS, RUC
    }

    public enum CustomerType {
        NATURAL, JURIDICO
    }
}
