package com.anyorder.pos.anyorder.controller;

import com.anyorder.pos.anyorder.model.Reservation;
import com.anyorder.pos.anyorder.service.ReservationService;
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
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservaciones", description = "Gestión de reservaciones de mesas")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Listar todas las reservaciones", description = "Obtiene todas las reservaciones registradas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    })
    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @Operation(summary = "Obtener reservación por ID", description = "Busca una reservación específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación encontrada"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(
            @Parameter(description = "ID de la reservación", required = true) @PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @Operation(summary = "Crear nueva reservación", description = "Registra una nueva reservación en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservación creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o mesa no disponible")
    })
    @PostMapping
    public ResponseEntity<Reservation> create(@Valid @RequestBody Reservation reservation) {
        Reservation created = reservationService.create(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar reservación", description = "Modifica los datos de una reservación existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservación actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> update(
            @Parameter(description = "ID de la reservación", required = true) @PathVariable Integer id,
            @Valid @RequestBody Reservation reservation) {
        Reservation updated = reservationService.update(id, reservation);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar reservación", description = "Elimina una reservación del sistema")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Integer id) {
        reservationService.delete(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Reservación eliminada correctamente");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirmar reservación", description = "Cambia el estado de la reservación a CONFIRMADA")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Reservation> confirm(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    @Operation(summary = "Cancelar reservación", description = "Cambia el estado de la reservación a CANCELADA")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancel(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(reservationService.cancelReservation(id, reason));
    }

    @Operation(summary = "Completar reservación", description = "Marca la reservación como COMPLETADA")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<Reservation> complete(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.completeReservation(id));
    }

    @Operation(summary = "Marcar como no presentado", description = "Marca la reservación como NO_SHOW")
    @PatchMapping("/{id}/no-show")
    public ResponseEntity<Reservation> markAsNoShow(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.markAsNoShow(id));
    }

    @Operation(summary = "Obtener reservaciones por estado", description = "Lista reservaciones según su estado")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Reservation>> getByStatus(
            @PathVariable Reservation.ReservationStatus status) {
        return ResponseEntity.ok(reservationService.findByStatus(status));
    }

    @Operation(summary = "Obtener reservaciones de un cliente", description = "Lista todas las reservaciones de un cliente específico")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Reservation>> getByCustomer(@PathVariable Integer customerId) {
        return ResponseEntity.ok(reservationService.findByCustomer(customerId));
    }

    @Operation(summary = "Obtener reservaciones de una mesa", description = "Lista todas las reservaciones de una mesa específica")
    @GetMapping("/table/{tableId}")
    public ResponseEntity<List<Reservation>> getByTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(reservationService.findByTable(tableId));
    }

    @Operation(summary = "Obtener reservaciones por rango de fechas", description = "Lista reservaciones entre dos fechas")
    @GetMapping("/date-range")
    public ResponseEntity<List<Reservation>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reservationService.findByDateRange(startDate, endDate));
    }

    @Operation(summary = "Obtener reservaciones pendientes", description = "Lista todas las reservaciones con estado PENDIENTE")
    @GetMapping("/pending")
    public ResponseEntity<List<Reservation>> getPending() {
        return ResponseEntity.ok(reservationService.findPendingReservations());
    }

    @Operation(summary = "Obtener reservaciones confirmadas de hoy", description = "Lista reservaciones confirmadas para el día actual")
    @GetMapping("/today")
    public ResponseEntity<List<Reservation>> getTodayConfirmed() {
        return ResponseEntity.ok(reservationService.findTodayConfirmedReservations());
    }

    @Operation(summary = "Obtener próximas reservaciones", description = "Lista las próximas N reservaciones")
    @GetMapping("/upcoming")
    public ResponseEntity<List<Reservation>> getUpcoming(
            @RequestParam(defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(reservationService.findUpcomingReservations(limit));
    }

    @Operation(summary = "Buscar reservaciones por nombre", description = "Busca reservaciones por nombre del cliente")
    @GetMapping("/search/name")
    public ResponseEntity<List<Reservation>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(reservationService.searchByCustomerName(name));
    }

    @Operation(summary = "Buscar reservaciones por teléfono", description = "Busca reservaciones por teléfono del cliente")
    @GetMapping("/search/phone")
    public ResponseEntity<List<Reservation>> searchByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(reservationService.searchByPhone(phone));
    }

    @Operation(summary = "Contar reservaciones activas por mesa", description = "Retorna el número de reservaciones activas de una mesa")
    @GetMapping("/count/table/{tableId}")
    public ResponseEntity<Map<String, Long>> countActiveByTable(@PathVariable Integer tableId) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", reservationService.countActiveReservationsByTable(tableId));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Contar reservaciones por estado", description = "Retorna el número de reservaciones según estado")
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Map<String, Long>> countByStatus(
            @PathVariable Reservation.ReservationStatus status) {
        Map<String, Long> response = new HashMap<>();
        response.put("total", reservationService.countByStatus(status));
        return ResponseEntity.ok(response);
    }
}