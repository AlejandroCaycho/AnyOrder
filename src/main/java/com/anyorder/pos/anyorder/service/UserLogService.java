package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.UserLog;
import com.anyorder.pos.anyorder.model.Users;
import com.anyorder.pos.anyorder.repository.UserLogRepository;
import com.anyorder.pos.anyorder.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLogService {

    private final UserLogRepository userLogRepository;
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public List<UserLog> findAll() {
        log.debug("Obteniendo todos los registros de usuario");
        return userLogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public UserLog findById(Integer id) {
        log.debug("Buscando log con ID: {}", id);
        return userLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByUserId(Integer userId) {
        log.debug("Obteniendo logs del usuario: {}", userId);
        return userLogRepository.findByUser_IdUser(userId);
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByState(Integer state) {
        log.debug("Obteniendo logs con estado: {}", state);
        return userLogRepository.findByState(state);
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByFecha(LocalDateTime fecha) {
        log.debug("Obteniendo logs de la fecha: {}", fecha);
        return userLogRepository.findByFecha(fecha);
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo logs entre {} y {}", startDate, endDate);
        return userLogRepository.findByDateRange(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByUserAndDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Obteniendo logs del usuario {} entre {} y {}", userId, startDate, endDate);
        return userLogRepository.findByUserAndDateRange(userId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<UserLog> findTodayLogs() {
        log.debug("Obteniendo logs del día");
        return userLogRepository.findTodayLogs();
    }

    @Transactional(readOnly = true)
    public List<UserLog> findActiveUsers() {
        log.debug("Obteniendo usuarios actualmente trabajando");
        return userLogRepository.findActiveUsers();
    }

    @Transactional(readOnly = true)
    public List<UserLog> findByStateAndFecha(Integer state, LocalDateTime fecha) {
        log.debug("Obteniendo logs con estado {} en fecha {}", state, fecha);
        return userLogRepository.findByStateAndFecha(state, fecha);
    }

    /**
     * Registrar entrada del usuario
     */
    @Transactional
    public UserLog registrarEntrada(Integer userId) {
        log.info("Registrando entrada del usuario: {}", userId);

        // Validar que no tenga una sesión activa
        userLogRepository.findActiveSession(userId).ifPresent(activeLog -> {
            throw new RuntimeException("El usuario ya tiene una sesión activa desde: " + activeLog.getHoraEntrada());
        });

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        // CAMBIO: Renombrar variable para evitar conflicto con el logger
        UserLog userLog = new UserLog();
        userLog.setUser(user);
        userLog.setFecha(LocalDateTime.now());
        userLog.setHoraEntrada(LocalDateTime.now());
        userLog.setHourlyRate(user.getHourlyRate());
        userLog.setPlannedHours(user.getPlannedHours());
        userLog.setState(1); // Activo

        UserLog saved = userLogRepository.save(userLog);
        log.info("Entrada registrada con ID: {}", saved.getIdLog());
        return saved;
    }

    /**
     * Registrar salida del usuario
     */
    @Transactional
    public UserLog registrarSalida(Integer userId) {
        log.info("Registrando salida del usuario: {}", userId);

        UserLog activeLog = userLogRepository.findActiveSession(userId)
                .orElseThrow(() -> new RuntimeException("No hay una sesión activa para este usuario"));

        activeLog.registrarSalida();
        UserLog updated = userLogRepository.save(activeLog);

        log.info("Salida registrada. Horas trabajadas: {}", updated.getHoursWorked());
        return updated;
    }

    /**
     * Obtener sesión activa del usuario
     */
    @Transactional(readOnly = true)
    public UserLog getActiveSession(Integer userId) {
        log.debug("Buscando sesión activa del usuario: {}", userId);
        return userLogRepository.findActiveSession(userId)
                .orElse(null);
    }

    /**
     * Total de horas trabajadas por usuario
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalHoursWorked(Integer userId) {
        log.debug("Calculando total de horas trabajadas del usuario: {}", userId);
        BigDecimal total = userLogRepository.sumHoursWorkedByUser(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * Total de horas por usuario y rango
     */
    @Transactional(readOnly = true)
    public BigDecimal getHoursWorkedByDateRange(Integer userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Calculando horas del usuario {} entre {} y {}", userId, startDate, endDate);
        BigDecimal total = userLogRepository.sumHoursWorkedByUserAndDateRange(userId, startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long countByUser(Integer userId) {
        log.debug("Contando registros del usuario: {}", userId);
        return userLogRepository.countByUser_IdUser(userId);
    }
}