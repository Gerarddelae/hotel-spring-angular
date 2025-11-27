package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.booking.dto.CalendarEntryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio especializado para operaciones del calendario de reservas.
 */
public interface CalendarBookingService {

    /**
     * Obtiene todas las entradas del calendario (check-ins y check-outs)
     * dentro del rango de fechas especificado.
     *
     * @param start fecha de inicio del rango
     * @param end fecha de fin del rango
     * @return lista de entradas del calendario
     * @throws IllegalArgumentException si las fechas son inválidas
     */
    List<CalendarEntryDTO> getCalendarEntries(LocalDate start, LocalDate end);
}

