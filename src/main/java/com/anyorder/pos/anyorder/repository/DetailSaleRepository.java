package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.DetailSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DetailSaleRepository extends JpaRepository<DetailSale, Integer> {

    // Buscar detalles por ID de venta
    List<DetailSale> findBySale_IdSale(Integer saleId);

    // Obtener detalles de una venta ordenados por ID
    @Query("SELECT ds FROM DetailSale ds WHERE ds.sale.idSale = :saleId ORDER BY ds.idDetail ASC")
    List<DetailSale> findDetailsBySaleId(@Param("saleId") Integer saleId);

    // Sumar cantidad total de items de una venta
    @Query("SELECT SUM(ds.amount) FROM DetailSale ds WHERE ds.sale.idSale = :saleId")
    Integer sumAmountBySaleId(@Param("saleId") Integer saleId);

    // Sumar subtotal de una venta
    @Query("SELECT SUM(ds.subtotal) FROM DetailSale ds WHERE ds.sale.idSale = :saleId")
    BigDecimal sumSubtotalBySaleId(@Param("saleId") Integer saleId);

    // Contar detalles de una venta
    @Query("SELECT COUNT(ds) FROM DetailSale ds WHERE ds.sale.idSale = :saleId")
    long countBySaleId(@Param("saleId") Integer saleId);

    // Eliminar todos los detalles de una venta
    @Query("DELETE FROM DetailSale ds WHERE ds.sale.idSale = :saleId")
    void deleteBySaleId(@Param("saleId") Integer saleId);

    // Buscar detalles por presentación (para reportes)
    List<DetailSale> findByPresentation_IdPresentation(Integer presentationId);

    // Productos más vendidos (por cantidad)
    @Query("SELECT ds.presentation.idPresentation, ds.presentation.name, SUM(ds.amount) as total " +
            "FROM DetailSale ds " +
            "WHERE ds.sale.state = 2 " +
            "GROUP BY ds.presentation.idPresentation, ds.presentation.name " +
            "ORDER BY total DESC")
    List<Object[]> findTopSellingProducts();
}