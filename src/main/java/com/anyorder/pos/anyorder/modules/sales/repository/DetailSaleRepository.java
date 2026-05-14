package com.anyorder.pos.anyorder.modules.sales.repository;

import com.anyorder.pos.anyorder.modules.sales.model.DetailSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailSaleRepository extends JpaRepository<DetailSale, Integer> {
    List<DetailSale> findBySale_IdSale(Integer idSale);
}
