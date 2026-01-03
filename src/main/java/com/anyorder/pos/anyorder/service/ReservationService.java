package com.anyorder.pos.anyorder.service;

import com.anyorder.pos.anyorder.model.Reservation;
import com.anyorder.pos.anyorder.model.Tables;
import com.anyorder.pos.anyorder.model.Customer;
import com.anyorder.pos.anyorder.model.Users;
import com.anyorder.pos.anyorder.repository.ReservationRepository;
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
    private final UsersService usersService;

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        log.debug("Obteniendo todas las reservaciones");
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation findById(Integer id) {
        log.debug("Buscando reservación con ID: {}", id);
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservación no encontrada con ID: " + id));
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        log.info("Creando nueva reservación");

        // Validar número de personas
        if (reservation.getNumberOfPeople() == null || reservation.getNumberOfPeople() < 1) {
            throw new RuntimeException("Debe haber al menos 1 persona");
        }
        if (reservation.getNumberOfPeople() > 50) {
            throw new RuntimeException("El número de personas no puede exceder 50");
        }

        // Validar que el cliente existe
        if (reservation.getCustomer() == null || reservation.getCustomer().getIdCustomer() == null) {
            throw new RuntimeException("El cliente es obligatorio");
        }
        Customer customer = customerService.findById(reservation.getCustomer().getIdCustomer());
        reservation.setCustomer(customer);

        // Copiar automáticamente nombre y teléfono del cliente
        reservation.setCustomerName(customer.getFirstName() + " " + customer.getLastName());
        reservation.setCustomerPhone(customer.getPhone());

        // Validar que la mesa existe y está activa
        if (reservation.getTable() == null || reservation.getTable().getIdTable() == null) {
            throw new RuntimeException("La mesa es obligatoria");
        }
        Tables table = tablesService.findById(reservation.getTable().getIdTable());
        if (!table.getState()) {
            throw new RuntimeException("La mesa seleccionada no está activa");
        }

        // Validar capacidad de la mesa
        if (reservation.getNumberOfPeople() > table.getCapacity()) {
            throw new RuntimeException(
                    "El número de personas (" + reservation.getNumberOfPeople() +
                            ") excede la capacidad de la mesa (" + table.getCapacity() + ")");
        }
        reservation.setTable(table);

        // Validar que el usuario creador existe
        if (reservation.getCreatedBy() == null || reservation.getCreatedBy().getIdUser() == null) {
            throw new RuntimeException("El usuario creador es obligatorio");
        }
        Users user = usersService.getUserById(reservation.getCreatedBy().getIdUser())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        reservation.setCreatedBy(user);

        // Validar fecha de reservación
        if (reservation.getReservationDate() == null) {
            throw new RuntimeException("La fecha de reservación es obligatoria");
        }
        if (reservation.getReservationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La fecha de reservación no puede ser en el pasado");
        }

        // Verificar disponibilidad de la mesa en la fecha solicitada
        List<Reservation> conflictingReservations = reservationRepository
                .findActiveReservationsByTableAndDateRange(
                        table.getIdTable(),
                        reservation.getReservationDate().minusHours(2),
                        reservation.getReservationDate().plusHours(2));

        if (!conflictingReservations.isEmpty()) {
            log.warn("Mesa {} ya tiene reservaciones cercanas a la fecha solicitada", table.getIdTable());
            throw new RuntimeException(
                    "La mesa ya tiene reservaciones cercanas a esta hora. " +
                            "Por favor seleccione otra hora o mesa.");
        }

        // Establecer estado inicial
        if (reservation.getReservationStatus() == null) {
            reservation.setReservationStatus(Reservation.ReservationStatus.PENDIENTE);
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservación creada exitosamente con ID: {}", savedReservation.getIdReservation());

        return savedReservation;
    }

    @Transactional
    public Reservation update(Integer id, Reservation reservationData) {
        log.info("Actualizando reservación con ID: {}", id);

        Reservation reservation = findById(id);

        // No permitir actualizar reservaciones canceladas o completadas
        if (reservation.getReservationStatus() == Reservation.ReservationStatus.CANCELADA ||
                reservation.getReservationStatus() == Reservation.ReservationStatus.COMPLETADA) {
            throw new RuntimeException(
                    "No se pueden modificar reservaciones canceladas o completadas");
        }

        // Actualizar mesa si cambió
        if (reservationData.getTable() != null &&
                !reservation.getTable().getIdTable().equals(reservationData.getTable().getIdTable())) {

            Tables newTable = tablesService.findById(reservationData.getTable().getIdTable());
            if (!newTable.getState()) {
                throw new RuntimeException("La mesa seleccionada no está activa");
            }

            if (reservationData.getNumberOfPeople() > newTable.getCapacity()) {
                throw new RuntimeException(
                        "El número de personas excede la capacidad de la nueva mesa");
            }

            reservation.setTable(newTable);
        }

        // Actualizar número de personas
        if (reservationData.getNumberOfPeople() != null) {
            if (reservationData.getNumberOfPeople() < 1 || reservationData.getNumberOfPeople() > 50) {
                throw new RuntimeException("El número de personas debe estar entre 1 y 50");
            }
            if (reservationData.getNumberOfPeople() > reservation.getTable().getCapacity()) {
                throw new RuntimeException(
                        "El número de personas excede la capacidad de la mesa");
            }
            reservation.setNumberOfPeople(reservationData.getNumberOfPeople());
        }

        // Actualizar fecha si cambió
        if (reservationData.getReservationDate() != null &&
                !reservation.getReservationDate().equals(reservationData.getReservationDate())) {

            if (reservationData.getReservationDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("La nueva fecha no puede ser en el pasado");
            }
            reservation.setReservationDate(reservationData.getReservationDate());
        }

        // Actualizar información del cliente si cambió el cliente
        if (reservationData.getCustomer() != null &&
                !reservation.getCustomer().getIdCustomer().equals(reservationData.getCustomer().getIdCustomer())) {

            Customer newCustomer = customerService.findById(reservationData.getCustomer().getIdCustomer());
            reservation.setCustomer(newCustomer);

            // Actualizar automáticamente nombre y teléfono del nuevo cliente
            reservation.setCustomerName(newCustomer.getFirstName() + " " + newCustomer.getLastName());
            reservation.setCustomerPhone(newCustomer.getPhone());
        }

        // Actualizar solicitudes especiales y notas
        if (reservationData.getSpecialRequests() != null) {
            reservation.setSpecialRequests(reservationData.getSpecialRequests());
        }

        if (reservationData.getNotes() != null) {
            reservation.setNotes(reservationData.getNotes());
        }

        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Reservación {} actualizada exitosamente", id);

        return updatedReservation;
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Eliminando reservación con ID: {}", id);
        if (!reservationRepository.existsById(id)) {
            throw new RuntimeException("Reservación no encontrada con ID: " + id);
        }
        reservationRepository.deleteById(id);
        log.info("Reservación {} eliminada exitosamente", id);
    }

    @Transactional
    public Reservation confirmReservation(Integer id) {
        log.info("Confirmando reservación con ID: {}", id);

        Reservation reservation = findById(id);

        if (reservation.getReservationStatus() != Reservation.ReservationStatus.PENDIENTE) {
            throw new RuntimeException("Solo se pueden confirmar reservaciones pendientes");
        }

        reservation.setReservationStatus(Reservation.ReservationStatus.CONFIRMADA);
        Reservation confirmed = reservationRepository.save(reservation);

        log.info("Reservación {} confirmada exitosamente", id);
        return confirmed;
    }

    @Transactional
    public Reservation cancelReservation(Integer id, String reason) {
        log.info("Cancelando reservación con ID: {}", id);

        Reservation reservation = findById(id);

        if (reservation.getReservationStatus() == Reservation.ReservationStatus.COMPLETADA) {
            throw new RuntimeException("No se pueden cancelar reservaciones completadas");
        }

        if (reservation.getReservationStatus() == Reservation.ReservationStatus.CANCELADA) {
            throw new RuntimeException("La reservación ya está cancelada");
        }

        reservation.setReservationStatus(Reservation.ReservationStatus.CANCELADA);
        if (reason != null && !reason.trim().isEmpty()) {
            String currentNotes = reservation.getNotes() != null ? reservation.getNotes() : "";
            reservation.setNotes(currentNotes + "\nMotivo cancelación: " + reason);
        }

        Reservation cancelled = reservationRepository.save(reservation);
        log.info("Reservación {} cancelada exitosamente", id);

        return cancelled;
    }

    @Transactional
    public Reservation completeReservation(Integer id) {
        log.info("Completando reservación con ID: {}", id);

        Reservation reservation = findById(id);

        if (reservation.getReservationStatus() != Reservation.ReservationStatus.CONFIRMADA) {
            throw new RuntimeException("Solo se pueden completar reservaciones confirmadas");
        }

        reservation.setReservationStatus(Reservation.ReservationStatus.COMPLETADA);
        Reservation completed = reservationRepository.save(reservation);

        log.info("Reservación {} completada exitosamente", id);
        return completed;
    }

    @Transactional
    public Reservation markAsNoShow(Integer id) {
        log.info("Marcando reservación {} como no presentado", id);

        Reservation reservation = findById(id);

        if (reservation.getReservationStatus() != Reservation.ReservationStatus.CONFIRMADA) {
            throw new RuntimeException("Solo se pueden marcar como NO_SHOW las reservaciones confirmadas");
        }

        reservation.setReservationStatus(Reservation.ReservationStatus.NO_SHOW);
        Reservation noShow = reservationRepository.save(reservation);

        log.info("Reservación {} marcada como NO_SHOW", id);
        return noShow;
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByStatus(Reservation.ReservationStatus status) {
        log.debug("Buscando reservaciones con estado: {}", status);
        return reservationRepository.findByReservationStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByCustomer(Integer customerId) {
        log.debug("Buscando reservaciones del cliente: {}", customerId);
        customerService.findById(customerId);
        return reservationRepository.findReservationsByCustomerOrderByDate(customerId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByTable(Integer tableId) {
        log.debug("Buscando reservaciones de la mesa: {}", tableId);
        tablesService.findById(tableId);
        return reservationRepository.findByTable_IdTable(tableId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Buscando reservaciones entre {} y {}", startDate, endDate);
        return reservationRepository.findReservationsBetweenDates(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findPendingReservations() {
        log.debug("Buscando reservaciones pendientes");
        return reservationRepository.findPendingReservations(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Reservation> findTodayConfirmedReservations() {
        log.debug("Buscando reservaciones confirmadas para hoy");
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        return reservationRepository.findConfirmedReservationsForToday(startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<Reservation> findUpcomingReservations(Integer limit) {
        log.debug("Buscando próximas {} reservaciones", limit);
        return reservationRepository.findUpcomingReservations(LocalDateTime.now(), limit);
    }

    @Transactional(readOnly = true)
    public List<Reservation> searchByCustomerName(String customerName) {
        log.debug("Buscando reservaciones por nombre: {}", customerName);
        return reservationRepository.findByCustomerNameContainingIgnoreCase(customerName);
    }

    @Transactional(readOnly = true)
    public List<Reservation> searchByPhone(String phone) {
        log.debug("Buscando reservaciones por teléfono: {}", phone);
        return reservationRepository.findByCustomerPhone(phone);
    }

    @Transactional(readOnly = true)
    public long countActiveReservationsByTable(Integer tableId) {
        log.debug("Contando reservaciones activas de la mesa: {}", tableId);
        return reservationRepository.countActiveReservationsByTable(tableId);
    }

    @Transactional(readOnly = true)
    public long countByStatus(Reservation.ReservationStatus status) {
        log.debug("Contando reservaciones con estado: {}", status);
        return reservationRepository.countByStatus(status);
    }
}