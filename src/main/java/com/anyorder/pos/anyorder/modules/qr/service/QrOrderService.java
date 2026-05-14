package com.anyorder.pos.anyorder.modules.qr.service;

import com.anyorder.pos.anyorder.modules.qr.model.QrOrder;
import com.anyorder.pos.anyorder.modules.qr.repository.QrOrderRepository;
import com.anyorder.pos.anyorder.modules.qr.repository.QrSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QrOrderService {

    @Autowired
    private QrOrderRepository qrOrderRepository;

    @Autowired
    private QrSessionRepository qrSessionRepository;

    public List<QrOrder> findBySessionId(Integer idSession) {
        return qrOrderRepository.findBySession_IdSession(idSession);
    }

    @Transactional
    public QrOrder create(QrOrder order) {
        return qrOrderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Integer id, QrOrder.QrOrderStatus status) {
        qrOrderRepository.findById(id).ifPresent(o -> {
            o.setOrderStatus(status);
            qrOrderRepository.save(o);
        });
    }
}
