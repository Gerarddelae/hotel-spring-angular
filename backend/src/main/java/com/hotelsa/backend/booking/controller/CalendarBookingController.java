package com.hotelsa.backend.booking.controller;

import com.hotelsa.backend.booking.dto.CalendarEntryDTO;
import com.hotelsa.backend.booking.service.CalendarBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para el calendario de entradas y salidas.
 * Proporciona endpoints para consultar check-ins y check-outs en un rango de fechas.
 */
@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarBookingController {

    private final CalendarBookingService calendarBookingService;

    /**
     * Obtiene todas las reservas cuyo check-in o check-out esté dentro del rango especificado.
     *
     * Endpoint: GET /api/calendar/entries?start=YYYY-MM-DD&end=YYYY-MM-DD
     *
     * Requiere autenticación JWT y rol ROLE_USER mínimo.
     * El filtro de tenant se aplica automáticamente basado en el hotelId del token JWT.
     *
     * @param start fecha de inicio del rango (formato: YYYY-MM-DD)
     * @param end fecha de fin del rango (formato: YYYY-MM-DD)
     * @return lista de entradas del calendario con información de check-ins y check-outs
     */
    @GetMapping("/entries")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<CalendarEntryDTO>> getCalendarEntries(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,

            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end
    ) {
        log.info("📅 Solicitando entradas del calendario: start={}, end={}", start, end);

        List<CalendarEntryDTO> entries = calendarBookingService.getCalendarEntries(start, end);

        log.info("✅ Retornando {} entradas del calendario", entries.size());

        return ResponseEntity.ok(entries);
    }
}

