package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByStateTrue();

    List<Customer> findByStateFalse();

    List<Customer> findByState(Boolean state);

    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);

    List<Customer> findByLastNameContainingIgnoreCase(String lastName);

    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    List<Customer> findByCustomerType(Customer.CustomerType customerType);

    List<Customer> findByDocumentType(Customer.DocumentType documentType);

    List<Customer> findByLastPurchaseDateAfter(LocalDateTime date);

    long countByStateTrue();

    long countByCustomerType(Customer.CustomerType customerType);

    @Query("SELECT c FROM Customer c WHERE c.state = true AND c.totalPurchases > :minPurchases ORDER BY c.totalSpent DESC")
    List<Customer> findVIPCustomers(@Param("minPurchases") Integer minPurchases);

    @Query("SELECT c FROM Customer c WHERE c.state = true AND c.lastPurchaseDate IS NOT NULL ORDER BY c.lastPurchaseDate DESC LIMIT :limit")
    List<Customer> findRecentCustomers(@Param("limit") Integer limit);
}