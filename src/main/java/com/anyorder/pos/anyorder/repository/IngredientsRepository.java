package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientsRepository extends JpaRepository<Ingredients, Integer> {

    Optional<Ingredients> findByCode(String code);

    boolean existsByCode(String code);

    Optional<Ingredients> findByCodeIgnoreCase(String code);

    List<Ingredients> findByStateTrue();

    List<Ingredients> findByStateFalse();

    List<Ingredients> findByState(Boolean state);

    List<Ingredients> findByCategory(String category);

    List<Ingredients> findBySupplier_IdSupplier(Integer supplierId);

    List<Ingredients> findByNameContainingIgnoreCase(String name);

    List<Ingredients> findByCategoryContainingIgnoreCase(String category);

    List<Ingredients> findByUnitContainingIgnoreCase(String unit);

    @Query("SELECT i FROM Ingredients i WHERE i.quantity <= i.minStock AND i.state = true ORDER BY i.quantity ASC")
    List<Ingredients> findLowStockIngredients();

    @Query("SELECT i FROM Ingredients i WHERE i.expirationDate IS NOT NULL AND i.expirationDate <= CURRENT_DATE AND i.state = true ORDER BY i.expirationDate ASC")
    List<Ingredients> findExpiredIngredients();

    @Query(value = "SELECT * FROM Ingredients i WHERE i.EXPIRATION_DATE IS NOT NULL AND i.EXPIRATION_DATE <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND i.STATE = 1 ORDER BY i.EXPIRATION_DATE ASC", nativeQuery = true)
    List<Ingredients> findExpiringIngredientsNextWeek();

    @Query("SELECT i FROM Ingredients i WHERE i.supplier.idSupplier = :supplierId AND i.state = true ORDER BY i.name ASC")
    List<Ingredients> findActiveIngredientsBySupplier(@Param("supplierId") Integer supplierId);

    @Query("SELECT i FROM Ingredients i WHERE i.category = :category AND i.state = true ORDER BY i.name ASC")
    List<Ingredients> findActiveIngredientsByCategory(@Param("category") String category);

    @Query("SELECT COUNT(i) FROM Ingredients i WHERE i.quantity <= i.minStock AND i.state = true")
    long countLowStockIngredients();

    @Query("SELECT COUNT(i) FROM Ingredients i WHERE i.expirationDate IS NOT NULL AND i.expirationDate <= CURRENT_DATE AND i.state = true")
    long countExpiredIngredients();

    @Query(value = "SELECT COUNT(*) FROM Ingredients i WHERE i.EXPIRATION_DATE IS NOT NULL AND i.EXPIRATION_DATE <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND i.STATE = 1", nativeQuery = true)
    long countExpiringIngredientsNextWeek();

    @Query("SELECT DISTINCT i.category FROM Ingredients i WHERE i.state = true ORDER BY i.category ASC")
    List<String> findAllActiveCategories();

    @Query("SELECT DISTINCT i.unit FROM Ingredients i WHERE i.state = true ORDER BY i.unit ASC")
    List<String> findAllActiveUnits();

    long countByStateTrue();

    long countBySupplier_IdSupplier(Integer supplierId);
}