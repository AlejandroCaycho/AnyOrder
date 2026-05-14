package com.anyorder.pos.anyorder.modules.reservations.repository;

import com.anyorder.pos.anyorder.modules.reservations.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByCustomer_IdCustomer(Integer idCustomer);
    List<Reservation> findByTable_IdTable(Integer idTable);
    List<Reservation> findByReservationStatus(Reservation.ReservationStatus status);
}
