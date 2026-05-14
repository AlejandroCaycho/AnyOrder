package com.anyorder.pos.anyorder.modules.products.repository;

import com.anyorder.pos.anyorder.modules.products.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Optional<Product> findByName(String name);

    boolean existsByIdCategoryAndName(Integer idCategory, String name);

    boolean existsByIdCategoryAndNameAndIdProductNot(Integer idCategory, String name, Integer idProduct);

    List<Product> findByStateTrue();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.area WHERE p.state = true")
    List<Product> findAllActiveWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.presentations WHERE p.idCategory = :idCategory")
    List<Product> findByCategoryWithDetails(@Param("idCategory") Integer idCategory);

    List<Product> findByIdCategoryAndStateTrue(Integer idCategory);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.presentations WHERE p.idArea = :idArea")
    List<Product> findByAreaWithDetails(@Param("idArea") Integer idArea);

    List<Product> findByIdAreaAndStateTrue(Integer idArea);

    List<Product> findByNameContainingIgnoreCase(String name);

    long countByIdCategoryAndStateTrue(Integer idCategory);

    long countByIdArea(Integer idArea);

    @Query("SELECT p.idProduct FROM Product p WHERE p.idCategory = :idCategory")
    List<Integer> findIdsByCategory(@Param("idCategory") Integer idCategory);
}
