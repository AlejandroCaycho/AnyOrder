package com.anyorder.pos.anyorder.modules.qr.repository;

import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrSessionRepository extends JpaRepository<QrSession, Integer> {
    
    Optional<QrSession> findBySessionToken(String sessionToken);
    
    List<QrSession> findByTable_IdTableAndSessionStatus(Integer tableId, QrSession.SessionStatus status);
    
    List<QrSession> findBySessionStatus(QrSession.SessionStatus status);
    
    @Query("SELECT s FROM QrSession s WHERE s.sessionStatus = 'ACTIVA' AND s.expiresAt < :now")
    List<QrSession> findExpiredSessions(LocalDateTime now);
}
