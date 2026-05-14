package com.anyorder.pos.anyorder.modules.qr.repository;

import com.anyorder.pos.anyorder.modules.qr.model.QrSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QrSessionRepository extends JpaRepository<QrSession, Integer> {
    Optional<QrSession> findBySessionToken(String sessionToken);
}
