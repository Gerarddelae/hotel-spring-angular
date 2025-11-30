package com.hotelsa.backend.booking.exception;

import com.hotelsa.backend.common.exception.ResourceAccessDeniedException;

/**
 * Excepción lanzada cuando un usuario intenta acceder a una reserva
 * que no pertenece a su hotel.
 */
public class BookingAccessDeniedException extends ResourceAccessDeniedException {

    public BookingAccessDeniedException(Long bookingId) {
        super("Reserva", bookingId);
    }

    public BookingAccessDeniedException(String message) {
        super(message);
    }
}
