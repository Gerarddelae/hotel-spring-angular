package com.hotelsa.backend.booking.repository;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCheckOutDate(LocalDate date);

    List<Booking> findByCheckOutDateBefore(LocalDate date);

    List<Booking> findByCheckInDate(LocalDate date);

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByRoomIdAndStatus(Long roomId, BookingStatus status);

    List<Booking> findByCheckInDateBetween(LocalDate start, LocalDate end);

    // Comprueba si existe al menos una reserva (no CANCELLED) para la habitación que se solapa con el rango
    boolean existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelId(
            Long roomId,
            BookingStatus statusNot,
            LocalDate checkOut,
            LocalDate checkIn,
            Long hotelId
    );

    // Similar a la anterior pero excluyendo un booking (útil al validar disponibilidad al actualizar la misma reserva)
    boolean existsByRoomIdAndStatusNotAndCheckInDateLessThanAndCheckOutDateGreaterThanAndHotelIdAndIdNot(
            Long roomId,
            BookingStatus statusNot,
            LocalDate checkOut,
            LocalDate checkIn,
            Long hotelId,
            Long excludeId
    );

    // Dashboard queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    int countByStatus(@Param("status") BookingStatus status);

    @Query("""
            SELECT COUNT(DISTINCT b.guest.id)
            FROM Booking b
            WHERE b.status = 'CHECKED_IN'
            AND :today BETWEEN b.checkInDate AND b.checkOutDate
            """)
    int countActiveGuestsToday(@Param("today") LocalDate today);
}
