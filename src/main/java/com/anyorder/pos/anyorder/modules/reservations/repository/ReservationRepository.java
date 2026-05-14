package com.anyorder.pos.anyorder.modules.reservations.repository;

import com.anyorder.pos.anyorder.modules.reservations.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    
    List<Reservation> findByCustomer_IdCustomer(Integer idCustomer);
    
    List<Reservation> findByTable_IdTable(Integer idTable);
    
    List<Reservation> findByReservationStatus(Reservation.ReservationStatus status);
    
    @Query("SELECT r FROM Reservation r WHERE r.reservationDate BETWEEN :start AND :end ORDER BY r.reservationDate ASC")
    List<Reservation> findByDateRange(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM Reservation r WHERE r.table.idTable = :tableId AND r.reservationDate BETWEEN :start AND :end AND r.reservationStatus NOT IN ('CANCELADA', 'NO_SHOW')")
    List<Reservation> findConflicts(Integer tableId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByReservationDateAfterOrderByReservationDateAsc(LocalDateTime date);
}
