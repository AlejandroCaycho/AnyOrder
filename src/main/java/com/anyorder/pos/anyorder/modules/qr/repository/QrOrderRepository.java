package com.anyorder.pos.anyorder.modules.qr.repository;

import com.anyorder.pos.anyorder.modules.qr.model.QrOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrOrderRepository extends JpaRepository<QrOrder, Integer> {
    List<QrOrder> findBySession_IdSession(Integer idSession);
}
