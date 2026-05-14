package com.anyorder.pos.anyorder.modules.qr.service;

import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import com.anyorder.pos.anyorder.modules.qr.repository.QrSessionRepository;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anyorder.pos.anyorder.util.TokenGenerator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrSessionService {

    private final QrSessionRepository qrSessionRepository;
    private final TablesService tablesService;
    private final CustomerService customerService;

    @Transactional(readOnly = true)
    public List<QrSession> findAllActive() {
        return qrSessionRepository.findBySessionStatus(QrSession.SessionStatus.ACTIVA);
    }

    @Transactional
    public QrSession startSession(Integer idTable, String customerName, Integer numberOfPeople, Integer idCustomer) {
        log.info("Iniciando sesión QR en mesa ID: {}", idTable);

        Tables table = tablesService.findById(idTable)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        if (!table.getState()) {
            throw new RuntimeException("La mesa no está activa");
        }

        // Close any existing active session for this table
        List<QrSession> existingSessions = qrSessionRepository.findByTable_IdTableAndSessionStatus(idTable, QrSession.SessionStatus.ACTIVA);
        for (QrSession s : existingSessions) {
            s.setSessionStatus(QrSession.SessionStatus.CERRADA);
            s.setClosedAt(LocalDateTime.now());
            qrSessionRepository.save(s);
        }

        QrSession session = new QrSession();
        session.setTable(table);
        session.setCustomerName(customerName);
        session.setNumberOfPeople(numberOfPeople != null ? numberOfPeople : 1);
        session.setSessionToken(TokenGenerator.generateToken());
        session.setSessionStatus(QrSession.SessionStatus.ACTIVA);
        
        if (idCustomer != null) {
            session.setCustomer(customerService.findById(idCustomer).orElse(null));
        }

        // Update table occupancy if it's not already occupied (optional behavior)
        if (!table.getIsOccupied()) {
            tablesService.updateOccupancy(idTable, true, session.getNumberOfPeople());
        }

        return qrSessionRepository.save(session);
    }

    public Optional<QrSession> findByToken(String token) {
        Optional<QrSession> sessionOpt = qrSessionRepository.findBySessionToken(token);
        
        // Check for expiration
        if (sessionOpt.isPresent()) {
            QrSession session = sessionOpt.get();
            if (session.getSessionStatus() == QrSession.SessionStatus.ACTIVA && 
                session.getExpiresAt() != null && 
                session.getExpiresAt().isBefore(LocalDateTime.now())) {
                
                session.setSessionStatus(QrSession.SessionStatus.EXPIRADA);
                qrSessionRepository.save(session);
                return Optional.empty();
            }
        }
        
        return sessionOpt;
    }

    @Transactional
    public void closeSession(Integer id) {
        qrSessionRepository.findById(id).ifPresent(s -> {
            s.setSessionStatus(QrSession.SessionStatus.CERRADA);
            s.setClosedAt(LocalDateTime.now());
            qrSessionRepository.save(s);
        });
    }

    @Transactional
    public void expireOldSessions() {
        List<QrSession> expired = qrSessionRepository.findExpiredSessions(LocalDateTime.now());
        for (QrSession s : expired) {
            s.setSessionStatus(QrSession.SessionStatus.EXPIRADA);
            qrSessionRepository.save(s);
        }
    }
}
