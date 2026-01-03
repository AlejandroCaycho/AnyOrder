package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.UserLog;
import com.anyorder.pos.anyorder.service.UserLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-logs")
@RequiredArgsConstructor
@Tag(name = "User Logs", description = "Gestión de registros de entrada/salida de empleados")
public class UserLogController {

    private final UserLogService userLogService;

    @Operation(summary = "Listar todos los logs")
    @GetMapping
    public ResponseEntity<List<UserLog>> getAll() {
        return ResponseEntity.ok(userLogService.findAll());
    }

    @Operation(summary = "Obtener log por ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserLog> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userLogService.findById(id));
    }

    @Operation(summary = "Logs de un usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserLog>> getByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userLogService.findByUserId(userId));
    }

    @Operation(summary = "Logs por estado (0=Inactivo, 1=Activo, 2=Cerrado)")
    @GetMapping("/state/{state}")
    public ResponseEntity<List<UserLog>> getByState(@PathVariable Integer state) {
        return ResponseEntity.ok(userLogService.findByState(state));
    }

    @Operation(summary = "Logs por fecha")
    @GetMapping("/fecha")
    public ResponseEntity<List<UserLog>> getByFecha(@RequestParam LocalDateTime fecha) {
        return ResponseEntity.ok(userLogService.findByFecha(fecha));
    }

    @Operation(summary = "Logs por rango de fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<UserLog>> getByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(userLogService.findByDateRange(startDate, endDate));
    }

    @Operation(summary = "Logs de usuario por rango")
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<UserLog>> getByUserAndDateRange(
            @PathVariable Integer userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(userLogService.findByUserAndDateRange(userId, startDate, endDate));
    }

    @Operation(summary = "Logs del día")
    @GetMapping("/today")
    public ResponseEntity<List<UserLog>> getTodayLogs() {
        return ResponseEntity.ok(userLogService.findTodayLogs());
    }

    @Operation(summary = "Usuarios actualmente trabajando")
    @GetMapping("/active-users")
    public ResponseEntity<List<UserLog>> getActiveUsers() {
        return ResponseEntity.ok(userLogService.findActiveUsers());
    }

    @Operation(summary = "Logs por estado y fecha")
    @GetMapping("/state/{state}/fecha")
    public ResponseEntity<List<UserLog>> getByStateAndFecha(
            @PathVariable Integer state,
            @RequestParam LocalDateTime fecha) {
        return ResponseEntity.ok(userLogService.findByStateAndFecha(state, fecha));
    }

    @Operation(summary = "Registrar entrada", description = "Registra la hora de entrada de un empleado")
    @PostMapping("/entrada/{userId}")
    public ResponseEntity<?> registrarEntrada(@PathVariable Integer userId) {
        try {
            UserLog log = userLogService.registrarEntrada(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(log);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Registrar salida", description = "Registra la hora de salida y calcula horas trabajadas + pago")
    @PatchMapping("/salida/{userId}")
    public ResponseEntity<?> registrarSalida(@PathVariable Integer userId) {
        try {
            UserLog log = userLogService.registrarSalida(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("log", log);
            response.put("horasTrabajadas", log.getHoursWorked());
            response.put("pago", log.calcularPago());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener sesión activa")
    @GetMapping("/user/{userId}/active-session")
    public ResponseEntity<?> getActiveSession(@PathVariable Integer userId) {
        UserLog log = userLogService.getActiveSession(userId);
        if (log == null) {
            return ResponseEntity.ok(Map.of("active", false, "message", "No hay sesión activa"));
        }
        return ResponseEntity.ok(Map.of("active", true, "session", log));
    }

    @Operation(summary = "Total de horas trabajadas", description = "Total histórico de horas trabajadas por un usuario")
    @GetMapping("/user/{userId}/total-hours")
    public ResponseEntity<Map<String, BigDecimal>> getTotalHours(@PathVariable Integer userId) {
        return ResponseEntity.ok(Map.of("totalHours", userLogService.getTotalHoursWorked(userId)));
    }

    @Operation(summary = "Horas por rango de fechas")
    @GetMapping("/user/{userId}/hours-range")
    public ResponseEntity<Map<String, BigDecimal>> getHoursByRange(
            @PathVariable Integer userId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(Map.of("hours",
                userLogService.getHoursWorkedByDateRange(userId, startDate, endDate)));
    }

    @Operation(summary = "Contar registros de usuario")
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> countByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(Map.of("total", userLogService.countByUser(userId)));
    }
}