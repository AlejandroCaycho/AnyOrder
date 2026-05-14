package com.anyorder.pos.anyorder.modules.categories.repository;

import com.anyorder.pos.anyorder.modules.categories.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    List<Category> findByStateTrue();

    @Query("SELECT c FROM Category c WHERE c.state = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveOrderedByDisplayOrder();

    @Query("SELECT c FROM Category c WHERE c.section = :section AND c.state = true ORDER BY c.displayOrder ASC")
    List<Category> findBySectionOrderedByDisplayOrder(@Param("section") String section);

    List<Category> findByDeliveryTrue();

    List<Category> findBySectionContainingIgnoreCase(String section);

    long countByStateTrue();
}
