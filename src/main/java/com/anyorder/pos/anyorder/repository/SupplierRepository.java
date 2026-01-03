package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    Optional<Supplier> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<Supplier> findByEmail(String email);

    List<Supplier> findByStateTrue();

    List<Supplier> findByStateFalse();

    List<Supplier> findByState(Boolean state);

    List<Supplier> findByNameContainingIgnoreCase(String name);

    List<Supplier> findByContactNameContainingIgnoreCase(String contactName);

    List<Supplier> findByDocumentType(Supplier.DocumentType documentType);

    long countByStateTrue();
}