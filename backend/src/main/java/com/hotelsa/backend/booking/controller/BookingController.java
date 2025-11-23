package com.hotelsa.backend.booking.controller;

import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.dto.BookingAddonResponse;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Crea una nueva reserva para el hotel del usuario autenticado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO dto) {
        BookingResponseDTO createdBooking = bookingService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    /**
     * Obtiene los detalles de una reserva específica (solo si pertenece al hotel del usuario).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        BookingResponseDTO booking = bookingService.findById(id);
        return ResponseEntity.ok(booking);
    }

    /**
     * Lista todas las reservas del hotel del usuario autenticado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookings = bookingService.findAll();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Actualiza una reserva (solo si pertenece al hotel del usuario).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequestDTO dto
    ) {
        BookingResponseDTO updatedBooking = bookingService.update(id, dto);
        return ResponseEntity.ok(updatedBooking);
    }

    /**
     * Elimina (soft delete) una reserva del hotel del usuario autenticado.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene las reservas con check-out hoy.
     */
    @GetMapping("/checkouts/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsCheckingOutToday() {
        List<BookingResponseDTO> bookings = bookingService.getBookingsCheckingOutToday();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene las reservas vencidas (check-out antes de hoy).
     */
    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getExpiredBookings() {
        List<BookingResponseDTO> bookings = bookingService.getExpiredBookings();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene las reservas con check-in hoy.
     */
    @GetMapping("/checkins/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsStartingToday() {
        List<BookingResponseDTO> bookings = bookingService.getBookingsStartingToday();
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene todas las reservas de un huésped específico.
     */
    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByGuest(@PathVariable Long guestId) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByGuest(guestId);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene las reservas de una habitación con un estado específico.
     */
    @GetMapping("/room/{roomId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByRoomAndStatus(
            @PathVariable Long roomId,
            @PathVariable BookingStatus status
    ) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByRoomAndStatus(roomId, status);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Obtiene las reservas dentro de un rango de fechas.
     */
    @GetMapping("/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingResponseDTO>> getBookingsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsBetween(start, end);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Añade una lista de addons a una reserva (soft-insert de la relación).
     */
    @PostMapping("/{id}/addons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> addAddonsToBooking(
            @PathVariable("id") Long bookingId,
            @RequestBody List<com.hotelsa.backend.booking.dto.BookingAddonRequest> addonRequests
    ) {
        BookingResponseDTO response = bookingService.addAddonsToBooking(bookingId, addonRequests);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina (soft delete) una relación addon de una reserva.
     */
    @DeleteMapping("/{id}/addons/{addonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeAddonFromBooking(
            @PathVariable("id") Long bookingId,
            @PathVariable("addonId") Long addonId
    ) {
        bookingService.removeAddonFromBooking(bookingId, addonId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista los addons asociados a una reserva.
     */
    @GetMapping("/{id}/addons")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<BookingAddonResponse>> getAddonsFromBooking(@PathVariable("id") Long bookingId) {
        List<BookingAddonResponse> addons = bookingService.getAddonsFromBooking(bookingId);
        return ResponseEntity.ok(addons);
    }

    /**
     * Actualiza la cantidad de un addon específico en una reserva.
     */
    @PatchMapping("/{id}/addons/{addonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> updateAddonQuantity(
            @PathVariable("id") Long bookingId,
            @PathVariable("addonId") Long addonId,
            @Valid @RequestBody com.hotelsa.backend.booking.dto.UpdateAddonQuantityRequest request
    ) {
        BookingResponseDTO response = bookingService.updateAddonQuantity(bookingId, addonId, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    /**
     * Cancela una reserva cambiando su estado a CANCELLED.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable Long id) {
        BookingResponseDTO response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }
}
