package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Integer> {

    // Por usuario
    List<UserLog> findByUser_IdUser(Integer userId);

    // Por estado
    List<UserLog> findByState(Integer state);

    // Usuario con sesión activa
    @Query("SELECT ul FROM UserLog ul WHERE ul.user.idUser = :userId AND ul.state = 1 AND ul.horaSalida IS NULL")
    Optional<UserLog> findActiveSession(@Param("userId") Integer userId);

    // Logs por fecha
    @Query("SELECT ul FROM UserLog ul WHERE DATE(ul.fecha) = DATE(:fecha)")
    List<UserLog> findByFecha(@Param("fecha") LocalDateTime fecha);

    // Logs por rango de fechas
    @Query("SELECT ul FROM UserLog ul WHERE ul.fecha BETWEEN :startDate AND :endDate")
    List<UserLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Logs de usuario por rango
    @Query("SELECT ul FROM UserLog ul WHERE ul.user.idUser = :userId AND ul.fecha BETWEEN :startDate AND :endDate")
    List<UserLog> findByUserAndDateRange(@Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Total de horas trabajadas por usuario
    @Query("SELECT SUM(ul.hoursWorked) FROM UserLog ul WHERE ul.user.idUser = :userId AND ul.state = 2")
    BigDecimal sumHoursWorkedByUser(@Param("userId") Integer userId);

    // Total de horas por usuario y rango
    @Query("SELECT SUM(ul.hoursWorked) FROM UserLog ul WHERE ul.user.idUser = :userId AND ul.fecha BETWEEN :startDate AND :endDate AND ul.state = 2")
    BigDecimal sumHoursWorkedByUserAndDateRange(@Param("userId") Integer userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Logs del día actual
    @Query("SELECT ul FROM UserLog ul WHERE DATE(ul.fecha) = CURDATE()")
    List<UserLog> findTodayLogs();

    // Usuarios actualmente trabajando
    @Query("SELECT ul FROM UserLog ul WHERE ul.state = 1 AND ul.horaSalida IS NULL")
    List<UserLog> findActiveUsers();

    // Contar logs por usuario
    long countByUser_IdUser(Integer userId);

    // Logs por estado y fecha
    @Query("SELECT ul FROM UserLog ul WHERE ul.state = :state AND DATE(ul.fecha) = DATE(:fecha)")
    List<UserLog> findByStateAndFecha(@Param("state") Integer state, @Param("fecha") LocalDateTime fecha);
}