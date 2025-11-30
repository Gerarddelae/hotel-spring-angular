package com.hotelsa.backend.booking.repository;

import com.hotelsa.backend.booking.dto.CalendarEntryDTO;
import com.hotelsa.backend.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio especializado para consultas del calendario de entradas y salidas.
 * Separado del BookingRepository principal para mantener la cohesión.
 */
@Repository
public interface CalendarBookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Obtiene todas las reservas cuyo check-in o check-out esté dentro del rango especificado.
     * Solo devuelve la información mínima necesaria para el calendario.
     *
     * @param start fecha de inicio del rango (inclusive)
     * @param end fecha de fin del rango (inclusive)
     * @return lista de entradas del calendario
     */
    @Query("""
        SELECT new com.hotelsa.backend.booking.dto.CalendarEntryDTO(
            b.id,
            g.fullName,
            r.number,
            b.checkInDate,
            b.checkOutDate
        )
        FROM Booking b
        JOIN b.guest g
        JOIN b.room r
        WHERE b.deleted = false
        AND (
            (b.checkInDate BETWEEN :start AND :end)
            OR (b.checkOutDate BETWEEN :start AND :end)
        )
        ORDER BY b.checkInDate, b.checkOutDate
    """)
    List<CalendarEntryDTO> findCalendarEntries(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
}

