package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Presentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresentationRepository extends JpaRepository<Presentation, Integer> {

       List<Presentation> findByStateTrue();

       List<Presentation> findByIdProduct(Integer idProduct);

       List<Presentation> findByIdProductAndStateTrue(Integer idProduct);

       Optional<Presentation> findByName(String name);

       List<Presentation> findByNameContainingIgnoreCase(String name);

       boolean existsByName(String name);

       boolean existsByNameAndIdPresentationNot(String name, Integer idPresentation);

       @Query("SELECT p FROM Presentation p WHERE p.state = true")
       List<Presentation> findAllActiveWithDetails();

       @Query("SELECT p FROM Presentation p WHERE p.idProduct = :idProduct AND p.state = true")
       List<Presentation> findByProductWithDetails(@Param("idProduct") Integer idProduct);

       @Query("SELECT p FROM Presentation p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.state = true")
       List<Presentation> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                     @Param("maxPrice") BigDecimal maxPrice);

       @Query("SELECT p FROM Presentation p WHERE p.promoPrice IS NOT NULL AND p.state = true")
       List<Presentation> findWithPromoPrice();

       @Query("SELECT p FROM Presentation p WHERE p.idProduct IN (SELECT pr.idProduct FROM Product pr WHERE pr.idCategory = :idCategory) AND p.state = true")
       List<Presentation> findByCategoryId(@Param("idCategory") Integer idCategory);

       @Query("SELECT p FROM Presentation p WHERE p.idProduct IN (SELECT pr.idProduct FROM Product pr WHERE pr.idArea = :idArea) AND p.state = true")
       List<Presentation> findByAreaId(@Param("idArea") Integer idArea);

       @Query("SELECT p FROM Presentation p WHERE p.preparationTime <= :maxTime AND p.state = true")
       List<Presentation> findByMaxPreparationTime(@Param("maxTime") Integer maxTime);

       @Query("SELECT p FROM Presentation p WHERE p.state = true ORDER BY p.price DESC")
       List<Presentation> findTopByPrice();

       long countByIdProduct(Integer idProduct);

       long countByIdProductAndStateTrue(Integer idProduct);

       @Query("SELECT p FROM Presentation p WHERE p.deliveryPrice IS NOT NULL AND p.state = true")
       List<Presentation> findAvailableForDelivery();

       @Query("SELECT p FROM Presentation p WHERE p.takeoutPrice IS NOT NULL AND p.state = true")
       List<Presentation> findAvailableForTakeout();

       @Query("SELECT pr.idPresentation FROM Presentation pr WHERE pr.idProduct = :productId")
       List<Integer> findIdsByProduct(@Param("productId") Integer productId);
}