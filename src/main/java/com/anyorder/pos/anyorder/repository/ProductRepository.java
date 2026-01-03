package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

        List<Product> findByStateTrue();

        List<Product> findByIdCategory(Integer idCategory);

        List<Product> findByIdCategoryAndStateTrue(Integer idCategory);

        List<Product> findByIdArea(Integer idArea);

        List<Product> findByIdAreaAndStateTrue(Integer idArea);

        Optional<Product> findByName(String name);

        List<Product> findByNameContainingIgnoreCase(String name);

        boolean existsByIdCategoryAndName(Integer idCategory, String name);

        boolean existsByIdCategoryAndNameAndIdProductNot(Integer idCategory, String name, Integer idProduct);

        @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.state = true")
        List<Product> findAllActiveWithCategory();

        @Query("SELECT p FROM Product p JOIN FETCH p.area WHERE p.state = true")
        List<Product> findAllActiveWithArea();

        @Query("SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN FETCH p.category " +
                        "LEFT JOIN FETCH p.area " +
                        "WHERE p.state = true")
        List<Product> findAllActiveWithDetails();

        @Query("SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN FETCH p.category " +
                        "LEFT JOIN FETCH p.area " +
                        "WHERE p.idCategory = :idCategory AND p.state = true")
        List<Product> findByCategoryWithDetails(@Param("idCategory") Integer idCategory);

        @Query("SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN FETCH p.category " +
                        "LEFT JOIN FETCH p.area " +
                        "WHERE p.idArea = :idArea AND p.state = true")
        List<Product> findByAreaWithDetails(@Param("idArea") Integer idArea);

        long countByIdCategory(Integer idCategory);

        long countByIdCategoryAndStateTrue(Integer idCategory);

        long countByIdArea(Integer idArea);

        @Query("SELECT p.idProduct FROM Product p WHERE p.idCategory = :categoryId")
        List<Integer> findIdsByCategory(@Param("categoryId") Integer categoryId);
}