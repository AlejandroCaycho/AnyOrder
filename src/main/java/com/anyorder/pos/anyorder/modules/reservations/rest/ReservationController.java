package com.anyorder.pos.anyorder.modules.reservations.rest;

import com.anyorder.pos.anyorder.modules.reservations.model.Reservation;
import com.anyorder.pos.anyorder.modules.reservations.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<Reservation>> getUpcoming() {
        return ResponseEntity.ok(reservationService.findUpcoming());
    }

    @GetMapping("/range")
    public ResponseEntity<List<Reservation>> getByRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return ResponseEntity.ok(reservationService.findByDateRange(start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Reservation reservation) {
        Reservation created = reservationService.create(reservation);
        return ResponseEntity.status(201).body(Map.of(
            "message", "Reserva creada correctamente",
            "data", created
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> update(@PathVariable Integer id, @Valid @RequestBody Reservation reservation) {
        return ResponseEntity.ok(reservationService.update(id, reservation));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Reservation> updateStatus(
            @PathVariable Integer id,
            @RequestParam Reservation.ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        reservationService.cancel(id);
        return ResponseEntity.ok(Map.of("message", "Reserva cancelada correctamente"));
    }
}
