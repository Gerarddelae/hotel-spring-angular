package com.hotelsa.backend.booking.dto;

import java.time.LocalDate;

/**
 * DTO para representar una entrada en el calendario (check-in o check-out).
 * El frontend utilizará esta información para renderizar eventos en el calendario.
 */
public record CalendarEntryDTO(
    Long bookingId,
    String guestName,
    String roomNumber,
    LocalDate checkInDate,
    LocalDate checkOutDate
) {}

