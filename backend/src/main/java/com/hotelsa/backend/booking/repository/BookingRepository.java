package com.hotelsa.backend.booking.repository;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
