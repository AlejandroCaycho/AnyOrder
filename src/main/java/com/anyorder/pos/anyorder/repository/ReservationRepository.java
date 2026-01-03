package com.anyorder.pos.anyorder.repository;

import com.anyorder.pos.anyorder.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByReservationStatus(Reservation.ReservationStatus status);

    List<Reservation> findByCustomer_IdCustomer(Integer customerId);

    List<Reservation> findByTable_IdTable(Integer tableId);

    List<Reservation> findByCreatedBy_IdUser(Integer userId);

    List<Reservation> findByCustomerNameContainingIgnoreCase(String customerName);

    List<Reservation> findByCustomerPhone(String customerPhone);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate >= :startDate AND r.reservationDate <= :endDate ORDER BY r.reservationDate ASC")
    List<Reservation> findReservationsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Reservation r WHERE r.table.idTable = :tableId AND r.reservationDate >= :startDate AND r.reservationDate <= :endDate AND r.reservationStatus IN ('PENDIENTE', 'CONFIRMADA')")
    List<Reservation> findActiveReservationsByTableAndDateRange(
            @Param("tableId") Integer tableId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = 'PENDIENTE' AND r.reservationDate >= :now ORDER BY r.reservationDate ASC")
    List<Reservation> findPendingReservations(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = 'CONFIRMADA' AND r.reservationDate >= :startDate AND r.reservationDate <= :endDate ORDER BY r.reservationDate ASC")
    List<Reservation> findConfirmedReservationsForToday(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Reservation r WHERE r.customer.idCustomer = :customerId ORDER BY r.createdAt DESC")
    List<Reservation> findReservationsByCustomerOrderByDate(@Param("customerId") Integer customerId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.table.idTable = :tableId AND r.reservationStatus IN ('PENDIENTE', 'CONFIRMADA')")
    long countActiveReservationsByTable(@Param("tableId") Integer tableId);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.reservationStatus = :status")
    long countByStatus(@Param("status") Reservation.ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate >= :now ORDER BY r.reservationDate ASC LIMIT :limit")
    List<Reservation> findUpcomingReservations(
            @Param("now") LocalDateTime now,
            @Param("limit") Integer limit);

    @Query("SELECT r FROM Reservation r WHERE r.reservationDate BETWEEN :start AND :end AND r.reservationStatus IN ('PENDIENTE', 'CONFIRMADA') ORDER BY r.reservationDate ASC")
    List<Reservation> findActiveReservationsForDate(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}