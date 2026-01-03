package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.QrSession;
import com.anyorder.pos.anyorder.service.QrSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qr-sessions")
@RequiredArgsConstructor
@Tag(name = "Sesiones QR", description = "Gestión de sesiones QR para pedidos por mesa")
public class QrSessionController {

    private final QrSessionService qrSessionService;

    @Operation(summary = "Listar todas las sesiones QR", description = "Obtiene todas las sesiones QR registradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<QrSession>> getAll() {
        return ResponseEntity.ok(qrSessionService.findAll());
    }

    @Operation(summary = "Obtener sesión QR por ID", description = "Busca una sesión QR específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<QrSession> getById(
            @Parameter(description = "ID de la sesión", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(qrSessionService.findById(id));
    }

    @Operation(summary = "Obtener sesión QR por token", description = "Busca una sesión QR por su token único")
    @GetMapping("/token/{token}")
    public ResponseEntity<QrSession> getByToken(@PathVariable String token) {
        return ResponseEntity.ok(qrSessionService.findByToken(token));
    }

    @Operation(summary = "Crear nueva sesión QR", description = "Inicia una nueva sesión QR para una mesa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sesión creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o mesa ya tiene sesión activa")
    })
    @PostMapping
    public ResponseEntity<QrSession> create(@Valid @RequestBody QrSession qrSession) {
        QrSession created = qrSessionService.create(qrSession);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar sesión QR", description = "Modifica los datos de una sesión QR activa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<QrSession> update(
            @Parameter(description = "ID de la sesión", required = true) @PathVariable Integer id,
            @Valid @RequestBody QrSession qrSession) {
        QrSession updated = qrSessionService.update(id, qrSession);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar sesión QR", description = "Elimina una sesión QR del sistema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        qrSessionService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesión QR eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cerrar sesión QR", description = "Cierra una sesión QR activa y libera la mesa")
    @PatchMapping("/{id}/close")
    public ResponseEntity<QrSession> closeSession(@PathVariable Integer id) {
        return ResponseEntity.ok(qrSessionService.closeSession(id));
    }

    @Operation(summary = "Expirar sesión QR", description = "Marca una sesión QR como expirada")
    @PatchMapping("/{id}/expire")
    public ResponseEntity<QrSession> expireSession(@PathVariable Integer id) {
        return ResponseEntity.ok(qrSessionService.expireSession(id));
    }

    @Operation(summary = "Expirar sesiones antiguas", description = "Expira automáticamente sesiones con más de N horas")
    @PostMapping("/expire-old")
    public ResponseEntity<Map<String, String>> expireOldSessions(
            @RequestParam(defaultValue = "3") Integer hours) {
        qrSessionService.expireOldSessions(hours);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Sesiones antiguas expiradas correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener sesiones por estado", description = "Lista sesiones según su estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<QrSession>> getByStatus(
            @PathVariable QrSession.SessionStatus status) {
        return ResponseEntity.ok(qrSessionService.findByStatus(status));
    }

    @Operation(summary = "Obtener sesiones de una mesa", description = "Lista todas las sesiones de una mesa específica")
    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<QrSession>> getByTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(qrSessionService.findByTable(tableId));
    }

    @Operation(summary = "Obtener sesión activa de una mesa", description = "Obtiene la sesión QR activa actual de una mesa")
    @GetMapping("/table/{tableId}/active")
    public ResponseEntity<QrSession> getActiveSessionByTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(qrSessionService.findActiveSessionByTable(tableId));
    }

    @Operation(summary = "Obtener sesiones activas", description = "Lista todas las sesiones QR activas")
    @GetMapping("/active")
    public ResponseEntity<List<QrSession>> getActiveSessions() {
        return ResponseEntity.ok(qrSessionService.findActiveSessions());
    }

    @Operation(summary = "Obtener sesiones por rango de fechas", description = "Lista sesiones entre dos fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<QrSession>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(qrSessionService.findSessionsBetweenDates(startDate, endDate));
    }

    @Operation(summary = "Contar sesiones activas por mesa", description = "Retorna el número de sesiones activas de una mesa")
    @GetMapping("/count/table/{tableId}")
    public ResponseEntity<Map<String, Long>> countActiveByTable(@PathVariable Integer tableId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", qrSessionService.countActiveSessionsByTable(tableId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar sesiones por estado", description = "Retorna el número de sesiones según estado")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Map<String, Long>> countByStatus(
            @PathVariable QrSession.SessionStatus status) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", qrSessionService.countByStatus(status));
        return ResponseEntity.ok(response);
    }
}