package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.booking.dto.CalendarEntryDTO;
import com.hotelsa.backend.booking.repository.CalendarBookingRepository;
import com.hotelsa.backend.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementación del servicio de calendario de reservas.
 * Proporciona información sobre check-ins y check-outs dentro de un rango de fechas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarBookingServiceImpl implements CalendarBookingService {

    private final CalendarBookingRepository calendarBookingRepository;

    /**
     * Obtiene todas las entradas del calendario dentro del rango especificado.
     * El filtro de tenant se aplica automáticamente a través del TenantFilter y HibernateFilterAspect.
     *
     * @param start fecha de inicio del rango
     * @param end fecha de fin del rango
     * @return lista de entradas del calendario
     * @throws BadRequestException si las fechas son inválidas
     */
    @Override
    @Transactional(readOnly = true)
    public List<CalendarEntryDTO> getCalendarEntries(LocalDate start, LocalDate end) {
        log.debug("Obteniendo entradas del calendario desde {} hasta {}", start, end);

        // Validar parámetros
        validateDateRange(start, end);

        // Ejecutar consulta - el filtro de tenant se aplica automáticamente
        List<CalendarEntryDTO> entries = calendarBookingRepository.findCalendarEntries(start, end);

        log.debug("Se encontraron {} entradas en el calendario", entries.size());

        return entries;
    }

    /**
     * Valida que el rango de fechas sea coherente.
     *
     * @param start fecha de inicio
     * @param end fecha de fin
     * @throws BadRequestException si alguna fecha es nula o start > end
     */
    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null) {
            throw new BadRequestException("La fecha de inicio (start) es obligatoria");
        }

        if (end == null) {
            throw new BadRequestException("La fecha de fin (end) es obligatoria");
        }

        if (start.isAfter(end)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }
}

