package com.anyorder.pos.anyorder.modules.customers.repository;

import com.anyorder.pos.anyorder.modules.customers.model.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    @EntityGraph(attributePaths = {"role"})
    List<Customer> findAll();

    @EntityGraph(attributePaths = {"role"})
    Optional<Customer> findById(Integer id);

    @EntityGraph(attributePaths = {"role"})
    Optional<Customer> findByDocumentNumber(String documentNumber);

    boolean existsByDocumentNumber(String documentNumber);

    @EntityGraph(attributePaths = {"role"})
    List<Customer> findByStateTrue();
}
