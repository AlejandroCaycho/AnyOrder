package com.anyorder.pos.anyorder.modules.suppliers.repository;

import com.anyorder.pos.anyorder.modules.suppliers.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findByDocumentNumber(String documentNumber);
    boolean existsByDocumentNumber(String documentNumber);
    List<Supplier> findByStateTrue();
}
