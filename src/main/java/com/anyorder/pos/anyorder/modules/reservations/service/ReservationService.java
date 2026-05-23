package com.anyorder.pos.anyorder.modules.reservations.service;

import com.anyorder.pos.anyorder.modules.reservations.model.Reservation;
import com.anyorder.pos.anyorder.modules.reservations.repository.ReservationRepository;
import com.anyorder.pos.anyorder.modules.customers.service.CustomerService;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import com.anyorder.pos.anyorder.modules.tables.model.Tables;
import com.anyorder.pos.anyorder.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerService customerService;
    private final TablesService tablesService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation findById(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservación no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByDateRange(start, end);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findUpcoming() {
        return reservationRepository.findByReservationDateAfterOrderByReservationDateAsc(LocalDateTime.now());
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        log.info("Creando nueva reservación para cliente: {}", reservation.getCustomerName());

        validateBasicData(reservation);
        checkTableAvailability(reservation);

        reservation.setReservationStatus(Reservation.ReservationStatus.PENDIENTE);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(Integer id, Reservation.ReservationStatus status) {
        Reservation reservation = findById(id);
        
        // Logic to free table or update occupancy if reservation is completed/started could go here
        
        reservation.setReservationStatus(status);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation update(Integer id, Reservation updated) {
        Reservation existing = findById(id);
        existing.setCustomerName(updated.getCustomerName());
        existing.setCustomerPhone(updated.getCustomerPhone());
        existing.setNumberOfPeople(updated.getNumberOfPeople());
        existing.setReservationDate(updated.getReservationDate());
        existing.setTable(updated.getTable());
        existing.setNotes(updated.getNotes());
        existing.setSpecialRequests(updated.getSpecialRequests());
        return reservationRepository.save(existing);
    }

    @Transactional
    public void cancel(Integer id) {
        Reservation reservation = findById(id);
        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELADA);
        reservationRepository.save(reservation);
    }

    private void validateBasicData(Reservation r) {
        if (r.getCustomer() == null || r.getCustomer().getIdCustomer() == null) {
            throw new RuntimeException("El cliente es obligatorio");
        }
        if (r.getTable() == null || r.getTable().getIdTable() == null) {
            throw new RuntimeException("La mesa es obligatoria");
        }
        if (r.getCreatedBy() == null || r.getCreatedBy().getIdUser() == null) {
            throw new RuntimeException("El usuario creador es obligatorio");
        }
        if (r.getReservationDate() == null || r.getReservationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha de reservación debe ser futura");
        }

        customerService.findById(r.getCustomer().getIdCustomer());
        userService.findById(r.getCreatedBy().getIdUser());
        
        Tables table = tablesService.findById(r.getTable().getIdTable())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        
        if (!table.getState()) {
            throw new RuntimeException("No se puede reservar una mesa inactiva");
        }

        if (!table.getArea().getState()) {
            throw new RuntimeException("El área seleccionada se encuentra inactiva");
        }

        if (table.getIsOccupied()) {
            throw new RuntimeException("La mesa ya se encuentra ocupada");
        }

        if (r.getNumberOfPeople() > table.getCapacity()) {
            throw new RuntimeException("El número de personas excede la capacidad de la mesa (" + table.getCapacity() + ")");
        }
    }

    private void checkTableAvailability(Reservation r) {
        // Simple overlap check: 2 hours buffer
        LocalDateTime start = r.getReservationDate().minusHours(2);
        LocalDateTime end = r.getReservationDate().plusHours(2);
        
        List<Reservation> conflicts = reservationRepository.findConflicts(r.getTable().getIdTable(), start, end);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("La mesa ya tiene una reservación cercana a esa hora");
        }
    }
}
