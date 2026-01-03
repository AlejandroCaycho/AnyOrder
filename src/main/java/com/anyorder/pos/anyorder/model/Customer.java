package com.anyorder.pos.anyorder.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @NotBlank
    @Column(name = "FIRST_NAME", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Column(name = "LAST_NAME", nullable = false, length = 100)
    private String lastName;

    @Email
    @Column(name = "EMAIL", length = 150)
    private String email;

    @JsonIgnore // CRÍTICO: NUNCA exponer password
    @Column(name = "PASSWORD", length = 255)
    private String password;

    @NotBlank
    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "DOCUMENT_TYPE", nullable = false)
    private DocumentType documentType;

    @NotBlank
    @Column(name = "DOCUMENT_NUMBER", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "CUSTOMER_TYPE", nullable = false)
    private CustomerType customerType = CustomerType.NATURAL;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROLE", nullable = false)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "idRole")
    @JsonIdentityReference(alwaysAsId = true)
    private Role role;

    @Column(name = "LAST_PURCHASE_DATE")
    private LocalDateTime lastPurchaseDate;

    @Column(name = "TOTAL_PURCHASES", nullable = false)
    private Integer totalPurchases = 0;

    @Column(name = "TOTAL_SPENT", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "STATE", nullable = false)
    private Boolean state = true;

    @Column(name = "CREATED_AT")
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