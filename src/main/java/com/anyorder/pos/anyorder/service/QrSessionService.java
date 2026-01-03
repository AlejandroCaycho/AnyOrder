package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Customer;
import com.anyorder.pos.anyorder.model.QrSession;
import com.anyorder.pos.anyorder.model.Tables;
import com.anyorder.pos.anyorder.repository.QrSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrSessionService {

    private final QrSessionRepository qrSessionRepository;
    private final TablesService tablesService;
    private final CustomerService customerService; // ✅ AGREGAR ESTA DEPENDENCIA

    @Transactional(readOnly = true)
    public List<QrSession> findAll() {
        log.debug("Obteniendo todas las sesiones QR");
        return qrSessionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public QrSession findById(Integer id) {
        log.debug("Buscando sesión QR con ID: {}", id);
        return qrSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sesión QR no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public QrSession findByToken(String token) {
        log.debug("Buscando sesión QR por token");
        return qrSessionRepository.findBySessionToken(token)
                .orElseThrow(() -> new RuntimeException("Sesión QR no encontrada con token: " + token));
    }

    @Transactional
    public QrSession create(QrSession qrSession) {
        log.info("Creando nueva sesión QR");

        // Validar mesa
        if (qrSession.getTable() == null || qrSession.getTable().getIdTable() == null) {
            throw new RuntimeException("La mesa es obligatoria");
        }

        Tables table = tablesService.findById(qrSession.getTable().getIdTable());
        if (!table.getState()) {
            throw new RuntimeException("La mesa no está activa");
        }

        // Verificar si la mesa ya tiene una sesión activa
        qrSessionRepository.findActiveSessionByTable(table.getIdTable())
                .ifPresent(activeSession -> {
                    throw new RuntimeException(
                            "La mesa " + table.getName() + " ya tiene una sesión QR activa. " +
                                    "Debe cerrar la sesión existente primero.");
                });

        qrSession.setTable(table);

        // ✅ VALIDAR Y ESTABLECER CUSTOMER SI VIENE
        if (qrSession.getCustomer() != null && qrSession.getCustomer().getIdCustomer() != null) {
            // findById ya lanza excepción si no existe
            Customer customer = customerService.findById(qrSession.getCustomer().getIdCustomer());
            qrSession.setCustomer(customer);

            // Si viene customer pero no customerName, usar el nombre del customer
            if (qrSession.getCustomerName() == null || qrSession.getCustomerName().trim().isEmpty()) {
                qrSession.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            }
        }

        // Validar número de personas
        if (qrSession.getNumberOfPeople() == null || qrSession.getNumberOfPeople() < 1) {
            qrSession.setNumberOfPeople(1);
        }
        if (qrSession.getNumberOfPeople() > 50) {
            throw new RuntimeException("El número de personas no puede exceder 50");
        }
        if (qrSession.getNumberOfPeople() > table.getCapacity()) {
            throw new RuntimeException(
                    "El número de personas (" + qrSession.getNumberOfPeople() +
                            ") excede la capacidad de la mesa (" + table.getCapacity() + ")");
        }

        // Generar token único si no existe
        if (qrSession.getSessionToken() == null || qrSession.getSessionToken().isEmpty()) {
            qrSession.setSessionToken(generateUniqueToken());
        } else {
            if (qrSessionRepository.existsBySessionToken(qrSession.getSessionToken())) {
                throw new RuntimeException("El token de sesión ya existe");
            }
        }

        // Validar longitud del token
        if (qrSession.getSessionToken().length() < 10) {
            throw new RuntimeException("El token debe tener al menos 10 caracteres");
        }

        // Establecer estado inicial
        if (qrSession.getSessionStatus() == null) {
            qrSession.setSessionStatus(QrSession.SessionStatus.ACTIVA);
        }

        // Ocupar la mesa
        tablesService.occupyTable(table.getIdTable(), qrSession.getNumberOfPeople());

        QrSession savedSession = qrSessionRepository.save(qrSession);
        log.info("Sesión QR creada exitosamente con ID: {}", savedSession.getIdSession());

        return savedSession;
    }

    @Transactional
    public QrSession update(Integer id, QrSession qrSessionData) {
        log.info("Actualizando sesión QR con ID: {}", id);

        QrSession qrSession = findById(id);

        // No permitir actualizar sesiones cerradas o expiradas
        if (qrSession.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("Solo se pueden actualizar sesiones activas");
        }

        // ✅ ACTUALIZAR CUSTOMER SI VIENE
        if (qrSessionData.getCustomer() != null && qrSessionData.getCustomer().getIdCustomer() != null) {
            // findById ya lanza excepción si no existe
            Customer customer = customerService.findById(qrSessionData.getCustomer().getIdCustomer());
            qrSession.setCustomer(customer);

            // Actualizar el nombre automáticamente
            if (qrSessionData.getCustomerName() == null || qrSessionData.getCustomerName().trim().isEmpty()) {
                qrSession.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
            }
        }

        // Actualizar nombre del cliente
        if (qrSessionData.getCustomerName() != null) {
            qrSession.setCustomerName(qrSessionData.getCustomerName());
        }

        // Actualizar número de personas
        if (qrSessionData.getNumberOfPeople() != null) {
            if (qrSessionData.getNumberOfPeople() < 1 || qrSessionData.getNumberOfPeople() > 50) {
                throw new RuntimeException("El número de personas debe estar entre 1 y 50");
            }
            if (qrSessionData.getNumberOfPeople() > qrSession.getTable().getCapacity()) {
                throw new RuntimeException(
                        "El número de personas excede la capacidad de la mesa");
            }

            // Actualizar ocupancia de la mesa
            tablesService.updateOccupancy(
                    qrSession.getTable().getIdTable(),
                    qrSessionData.getNumberOfPeople());

            qrSession.setNumberOfPeople(qrSessionData.getNumberOfPeople());
        }

        QrSession updatedSession = qrSessionRepository.save(qrSession);
        log.info("Sesión QR {} actualizada exitosamente", id);

        return updatedSession;
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando sesión QR con ID: {}", id);

        QrSession session = findById(id);

        // Liberar la mesa si la sesión está activa
        if (session.getSessionStatus() == QrSession.SessionStatus.ACTIVA) {
            tablesService.releaseTable(session.getTable().getIdTable());
        }

        qrSessionRepository.deleteById(id);
        log.info("Sesión QR {} eliminada exitosamente", id);
    }

    @Transactional
    public QrSession closeSession(Integer id) {
        log.info("Cerrando sesión QR con ID: {}", id);

        QrSession qrSession = findById(id);

        if (qrSession.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("Solo se pueden cerrar sesiones activas");
        }

        qrSession.setSessionStatus(QrSession.SessionStatus.CERRADA);
        qrSession.setClosedAt(LocalDateTime.now());

        // Liberar la mesa
        tablesService.releaseTable(qrSession.getTable().getIdTable());

        QrSession closedSession = qrSessionRepository.save(qrSession);
        log.info("Sesión QR {} cerrada exitosamente", id);

        return closedSession;
    }

    @Transactional
    public QrSession expireSession(Integer id) {
        log.info("Expirando sesión QR con ID: {}", id);

        QrSession qrSession = findById(id);

        if (qrSession.getSessionStatus() != QrSession.SessionStatus.ACTIVA) {
            throw new RuntimeException("Solo se pueden expirar sesiones activas");
        }

        qrSession.setSessionStatus(QrSession.SessionStatus.EXPIRADA);
        qrSession.setClosedAt(LocalDateTime.now());

        // Liberar la mesa
        tablesService.releaseTable(qrSession.getTable().getIdTable());

        QrSession expiredSession = qrSessionRepository.save(qrSession);
        log.info("Sesión QR {} expirada exitosamente", id);

        return expiredSession;
    }

    @Transactional
    public void expireOldSessions(int hoursToExpire) {
        log.info("Expirando sesiones QR con más de {} horas de antigüedad", hoursToExpire);

        LocalDateTime expirationTime = LocalDateTime.now().minusHours(hoursToExpire);
        List<QrSession> expiredSessions = qrSessionRepository.findExpiredSessions(expirationTime);

        for (QrSession session : expiredSessions) {
            try {
                expireSession(session.getIdSession());
            } catch (Exception e) {
                log.error("Error al expirar sesión {}: {}", session.getIdSession(), e.getMessage());
            }
        }

        log.info("{} sesiones QR expiradas", expiredSessions.size());
    }

    @Transactional(readOnly = true)
    public List<QrSession> findByStatus(QrSession.SessionStatus status) {
        log.debug("Buscando sesiones QR con estado: {}", status);
        return qrSessionRepository.findBySessionStatus(status);
    }

    @Transactional(readOnly = true)
    public List<QrSession> findByTable(Integer tableId) {
        log.debug("Buscando sesiones QR de la mesa: {}", tableId);
        tablesService.findById(tableId);
        return qrSessionRepository.findByTable_IdTable(tableId);
    }

    @Transactional(readOnly = true)
    public QrSession findActiveSessionByTable(Integer tableId) {
        log.debug("Buscando sesión QR activa de la mesa: {}", tableId);
        return qrSessionRepository.findActiveSessionByTable(tableId)
                .orElseThrow(() -> new RuntimeException(
                        "No hay sesión QR activa para la mesa con ID: " + tableId));
    }

    @Transactional(readOnly = true)
    public List<QrSession> findActiveSessions() {
        log.debug("Buscando sesiones QR activas");
        return qrSessionRepository.findAllActiveSessions();
    }

    @Transactional(readOnly = true)
    public List<QrSession> findSessionsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Buscando sesiones QR entre {} y {}", startDate, endDate);
        return qrSessionRepository.findSessionsBetweenDates(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public long countActiveSessionsByTable(Integer tableId) {
        log.debug("Contando sesiones activas de la mesa: {}", tableId);
        return qrSessionRepository.countActiveSessionsByTable(tableId);
    }

    @Transactional(readOnly = true)
    public long countByStatus(QrSession.SessionStatus status) {
        log.debug("Contando sesiones con estado: {}", status);
        return qrSessionRepository.countByStatus(status);
    }

    /**
     * Genera un token único para la sesión QR
     */
    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        } while (qrSessionRepository.existsBySessionToken(token));
        return token;
    }
}