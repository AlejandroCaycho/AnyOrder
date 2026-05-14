package com.anyorder.pos.anyorder.modules.sales.repository;

import com.anyorder.pos.anyorder.modules.sales.model.SalePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalePaymentRepository extends JpaRepository<SalePayment, Integer> {
    List<SalePayment> findBySale_IdSale(Integer idSale);
}
