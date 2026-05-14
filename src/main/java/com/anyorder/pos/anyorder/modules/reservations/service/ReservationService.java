package com.anyorder.pos.anyorder.modules.reservations.service;

import com.anyorder.pos.anyorder.modules.reservations.model.Reservation;
import com.anyorder.pos.anyorder.modules.reservations.repository.ReservationRepository;
import com.anyorder.pos.anyorder.modules.customers.service.CustomerService;
import com.anyorder.pos.anyorder.modules.tables.service.TablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TablesService tablesService;

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public Reservation create(Reservation reservation) {
        // Validation logic can be added here
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateStatus(Integer id, Reservation.ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));
        reservation.setReservationStatus(status);
        return reservationRepository.save(reservation);
    }
}
