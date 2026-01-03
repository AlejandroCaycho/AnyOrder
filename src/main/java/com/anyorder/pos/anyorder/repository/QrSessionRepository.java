package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QrSessionRepository extends JpaRepository<QrSession, Integer> {

    Optional<QrSession> findBySessionToken(String sessionToken);

    boolean existsBySessionToken(String sessionToken);

    List<QrSession> findBySessionStatus(QrSession.SessionStatus status);

    List<QrSession> findByTable_IdTable(Integer tableId);

    @Query("SELECT s FROM QrSession s WHERE s.table.idTable = :tableId AND s.sessionStatus = 'ACTIVA'")
    Optional<QrSession> findActiveSessionByTable(@Param("tableId") Integer tableId);

    @Query("SELECT s FROM QrSession s WHERE s.sessionStatus = 'ACTIVA' ORDER BY s.startedAt DESC")
    List<QrSession> findAllActiveSessions();

    @Query("SELECT s FROM QrSession s WHERE s.startedAt >= :startDate AND s.startedAt <= :endDate ORDER BY s.startedAt DESC")
    List<QrSession> findSessionsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM QrSession s WHERE s.table.idTable = :tableId AND s.sessionStatus = 'ACTIVA'")
    long countActiveSessionsByTable(@Param("tableId") Integer tableId);

    @Query("SELECT COUNT(s) FROM QrSession s WHERE s.sessionStatus = :status")
    long countByStatus(@Param("status") QrSession.SessionStatus status);

    @Query("SELECT s FROM QrSession s WHERE s.sessionStatus = 'ACTIVA' AND s.startedAt < :expirationTime")
    List<QrSession> findExpiredSessions(@Param("expirationTime") LocalDateTime expirationTime);
}