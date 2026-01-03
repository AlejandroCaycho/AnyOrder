package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {

    // Buscar por usuario
    List<Sale> findByUser_IdUser(Integer userId);

    // Buscar por cliente
    List<Sale> findByCustomer_IdCustomer(Integer customerId);

    // Buscar por pedido
    List<Sale> findByOrder_IdOrder(Integer orderId);

    // Buscar por estado
    List<Sale> findByState(Integer state);

    // Buscar ventas por rango de fechas
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Buscar ventas por estado y rango de fechas
    @Query("SELECT s FROM Sale s WHERE s.state = :state AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findByStateAndDateRange(
            @Param("state") Integer state,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Contar ventas por estado
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.state = :state")
    long countByState(@Param("state") Integer state);

    // Sumar total de ventas por rango de fechas
    @Query("SELECT SUM(s.order.total) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.state = 2")
    java.math.BigDecimal sumTotalByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Ventas del día actual
    @Query("SELECT s FROM Sale s WHERE DATE(s.saleDate) = CURRENT_DATE ORDER BY s.saleDate DESC")
    List<Sale> findTodaySales();

    // Ventas recientes (últimas N ventas)
    @Query("SELECT s FROM Sale s ORDER BY s.saleDate DESC LIMIT :limit")
    List<Sale> findRecentSales(@Param("limit") Integer limit);

    // Ventas de un usuario en un rango de fechas
    @Query("SELECT s FROM Sale s WHERE s.user.idUser = :userId AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findByUserAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Verificar si existe venta para un pedido
    boolean existsByOrder_IdOrder(Integer orderId);
}