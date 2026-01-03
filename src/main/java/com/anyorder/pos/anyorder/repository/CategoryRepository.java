package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByName(String name);

    List<Category> findByStateTrue();

    List<Category> findBySection(String section);

    List<Category> findByDeliveryTrue();

    List<Category> findBySectionAndStateTrue(String section);

    boolean existsByName(String name);

    boolean existsByNameAndIdCategoryNot(String name, Integer idCategory);

    @Query("SELECT c FROM Category c WHERE c.state = true ORDER BY c.displayOrder ASC")
    List<Category> findAllActiveOrderedByDisplayOrder();

    @Query("SELECT c FROM Category c WHERE c.section = :section AND c.state = true ORDER BY c.displayOrder ASC")
    List<Category> findBySectionOrderedByDisplayOrder(String section);

    @Query("SELECT p.idProduct FROM Product p WHERE p.idCategory = :categoryId")
    List<Integer> findIdsByCategory(@Param("categoryId") Integer categoryId);
}