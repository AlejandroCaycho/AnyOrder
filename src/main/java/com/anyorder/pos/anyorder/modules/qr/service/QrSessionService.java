package com.anyorder.pos.anyorder.modules.qr.service;

import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import com.anyorder.pos.anyorder.modules.qr.repository.QrSessionRepository;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import com.anyorder.pos.anyorder.modules.customers.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class QrSessionService {

    @Autowired
    private QrSessionRepository qrSessionRepository;

    @Autowired
    private TablesService tablesService;

    @Autowired
    private CustomerService customerService;

    @Transactional
    public QrSession startSession(Integer idTable, String customerName) {
        QrSession session = new QrSession();
        session.setTable(tablesService.findById(idTable).orElseThrow(() -> new RuntimeException("Mesa no encontrada")));
        session.setCustomerName(customerName);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setSessionStatus(QrSession.SessionStatus.ACTIVA);
        return qrSessionRepository.save(session);
    }

    public Optional<QrSession> findByToken(String token) {
        return qrSessionRepository.findBySessionToken(token);
    }

    @Transactional
    public void closeSession(Integer id) {
        qrSessionRepository.findById(id).ifPresent(s -> {
            s.setSessionStatus(QrSession.SessionStatus.CERRADA);
            s.setClosedAt(LocalDateTime.now());
            qrSessionRepository.save(s);
        });
    }
}
