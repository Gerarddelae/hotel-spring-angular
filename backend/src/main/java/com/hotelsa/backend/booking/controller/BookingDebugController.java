package com.hotelsa.backend.booking.controller;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador temporal de debug para diagnosticar el problema de huéspedes activos
 */
@RestController
@RequestMapping("/api/debug/bookings")
@RequiredArgsConstructor
public class BookingDebugController {

    private final BookingRepository bookingRepository;

    @GetMapping("/active-guests-debug")
    public ResponseEntity<Map<String, Object>> debugActiveGuests() {
        LocalDate today = LocalDate.now();

        Map<String, Object> debug = new HashMap<>();
        debug.put("today", today);

        // Contar por cada estado
        debug.put("totalBookings", bookingRepository.count());
        debug.put("pendingCount", bookingRepository.countByStatus(BookingStatus.PENDING));
        debug.put("confirmedCount", bookingRepository.countByStatus(BookingStatus.CONFIRMED));
        debug.put("checkedInCount", bookingRepository.countByStatus(BookingStatus.CHECKED_IN));
        debug.put("cancelledCount", bookingRepository.countByStatus(BookingStatus.CANCELLED));

        // Contar huéspedes activos con ambas firmas
        debug.put("activeGuestsCount", bookingRepository.countActiveGuestsToday(today, BookingStatus.CHECKED_IN));

        // Obtener todas las bookings CHECKED_IN
        var checkedInBookings = bookingRepository.findByRoomIdAndStatus(null, BookingStatus.CHECKED_IN);
        debug.put("checkedInBookingsList", checkedInBookings.size());

        // Listar bookings con CHECKED_IN y sus fechas
        var allBookings = bookingRepository.findAll();
        long checkedInInRange = allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_IN)
                .filter(b -> !today.isBefore(b.getCheckInDate()) && !today.isAfter(b.getCheckOutDate()))
                .count();
        debug.put("manualCount", checkedInInRange);

        return ResponseEntity.ok(debug);
    }
}

