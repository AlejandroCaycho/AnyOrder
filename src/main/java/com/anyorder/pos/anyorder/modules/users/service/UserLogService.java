package com.anyorder.pos.anyorder.modules.users.service;

import com.anyorder.pos.anyorder.modules.users.model.UserLog;
import com.anyorder.pos.anyorder.modules.users.model.User;
import com.anyorder.pos.anyorder.modules.users.repository.UserLogRepository;
import com.anyorder.pos.anyorder.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserLogRepository userLogRepository;
    private final UserRepository userRepository;

    public List<UserLog> findAll() {
        return userLogRepository.findAll();
    }

    public List<UserLog> findByUserId(Integer idUser) {
        return userLogRepository.findByUser_IdUser(idUser);
    }

    public Optional<UserLog> findActiveLog(Integer idUser) {
        return userLogRepository.findByUser_IdUserAndState(idUser, 1);
    }

    @Transactional
    public UserLog startLog(Integer idUser) {
        // Check if there is already an active log
        if (findActiveLog(idUser).isPresent()) {
            throw new RuntimeException("El usuario ya tiene una sesión activa");
        }

        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserLog log = new UserLog();
        log.setUser(user);
        log.setFecha(LocalDateTime.now());
        log.setHoraEntrada(LocalDateTime.now());
        log.setHourlyRate(user.getHourlyRate());
        log.setPlannedHours(user.getPlannedHours());
        log.setState(1); // Activo
        
        return userLogRepository.save(log);
    }

    @Transactional
    public UserLog endLog(Integer idUser) {
        UserLog log = findActiveLog(idUser)
                .orElseThrow(() -> new RuntimeException("No hay sesión activa para este usuario"));
        
        log.registrarSalida();
        return userLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalEarnings(Integer idUser) {
        return findByUserId(idUser).stream()
                .filter(l -> l.getState() == 2) // Cerrado
                .map(UserLog::calcularPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
