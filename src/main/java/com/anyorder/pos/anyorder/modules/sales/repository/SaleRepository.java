package com.anyorder.pos.anyorder.modules.sales.repository;

import com.anyorder.pos.anyorder.modules.sales.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Integer> {

    List<Sale> findByUser_IdUser(Integer idUser);

    List<Sale> findByCustomer_IdCustomer(Integer idCustomer);

    boolean existsByOrder_IdOrder(Integer idOrder);

    List<Sale> findByState(Integer state);

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end ORDER BY s.saleDate DESC")
    List<Sale> findByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s WHERE s.saleDate >= :startOfDay ORDER BY s.saleDate DESC")
    List<Sale> findTodaySales(LocalDateTime startOfDay);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.state = 1 AND s.saleDate BETWEEN :start AND :end")
    BigDecimal sumTotalByDateRange(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT d.presentation.name, SUM(d.amount) as totalSold
        FROM DetailSale d
        WHERE d.sale.state = 1
        GROUP BY d.presentation.idPresentation, d.presentation.name
        ORDER BY totalSold DESC
        """)
    List<Object[]> findMostSoldPresentations();
}
