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

    boolean existsByCategory_IdCategoryAndName(Integer idCategory, String name);

    boolean existsByCategory_IdCategoryAndNameAndIdProductNot(Integer idCategory, String name, Integer idProduct);

    List<Product> findByStateTrue();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.area WHERE p.state = true")
    List<Product> findAllActiveWithDetails();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.presentations WHERE p.category.idCategory = :idCategory")
    List<Product> findByCategoryWithDetails(@Param("idCategory") Integer idCategory);

    List<Product> findByCategory_IdCategoryAndStateTrue(Integer idCategory);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.presentations WHERE p.area.idArea = :idArea")
    List<Product> findByAreaWithDetails(@Param("idArea") Integer idArea);

    List<Product> findByArea_IdAreaAndStateTrue(Integer idArea);

    List<Product> findByNameContainingIgnoreCase(String name);

    long countByCategory_IdCategoryAndStateTrue(Integer idCategory);

    long countByArea_IdArea(Integer idArea);

    @Query("SELECT p.idProduct FROM Product p WHERE p.category.idCategory = :idCategory")
    List<Integer> findIdsByCategory(@Param("idCategory") Integer idCategory);
}
