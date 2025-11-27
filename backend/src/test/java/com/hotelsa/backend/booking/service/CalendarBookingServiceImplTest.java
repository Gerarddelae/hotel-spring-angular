package com.hotelsa.backend.booking.service;

import com.hotelsa.backend.booking.dto.CalendarEntryDTO;
import com.hotelsa.backend.booking.repository.CalendarBookingRepository;
import com.hotelsa.backend.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CalendarBookingServiceImpl.
 * Verifica la lógica de validación y la correcta invocación del repositorio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CalendarBookingServiceImpl Tests")
class CalendarBookingServiceImplTest {

    @Mock
    private CalendarBookingRepository calendarBookingRepository;

    @InjectMocks
    private CalendarBookingServiceImpl calendarBookingService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2025, 11, 1);
        endDate = LocalDate.of(2025, 11, 30);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay entradas en el calendario")
    void shouldReturnEmptyListWhenNoCalendarEntries() {
        // Given
        when(calendarBookingRepository.findCalendarEntries(startDate, endDate))
            .thenReturn(Collections.emptyList());

        // When
        List<CalendarEntryDTO> result = calendarBookingService.getCalendarEntries(startDate, endDate);

        // Then
        assertThat(result).isEmpty();
        verify(calendarBookingRepository, times(1)).findCalendarEntries(startDate, endDate);
    }

    @Test
    @DisplayName("Debe retornar entradas del calendario cuando existen reservas")
    void shouldReturnCalendarEntriesWhenBookingsExist() {
        // Given
        CalendarEntryDTO entry1 = new CalendarEntryDTO(
            1L,
            "John Doe",
            "101",
            LocalDate.of(2025, 11, 5),
            LocalDate.of(2025, 11, 10)
        );

        CalendarEntryDTO entry2 = new CalendarEntryDTO(
            2L,
            "Jane Smith",
            "203",
            LocalDate.of(2025, 11, 15),
            LocalDate.of(2025, 11, 20)
        );

        List<CalendarEntryDTO> expectedEntries = Arrays.asList(entry1, entry2);
        when(calendarBookingRepository.findCalendarEntries(startDate, endDate))
            .thenReturn(expectedEntries);

        // When
        List<CalendarEntryDTO> result = calendarBookingService.getCalendarEntries(startDate, endDate);

        // Then
        assertThat(result)
            .hasSize(2)
            .containsExactlyElementsOf(expectedEntries);
        verify(calendarBookingRepository, times(1)).findCalendarEntries(startDate, endDate);
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException cuando start es null")
    void shouldThrowExceptionWhenStartDateIsNull() {
        // When & Then
        assertThatThrownBy(() -> calendarBookingService.getCalendarEntries(null, endDate))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("fecha de inicio")
            .hasMessageContaining("obligatoria");

        verify(calendarBookingRepository, never()).findCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException cuando end es null")
    void shouldThrowExceptionWhenEndDateIsNull() {
        // When & Then
        assertThatThrownBy(() -> calendarBookingService.getCalendarEntries(startDate, null))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("fecha de fin")
            .hasMessageContaining("obligatoria");

        verify(calendarBookingRepository, never()).findCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("Debe lanzar BadRequestException cuando start es posterior a end")
    void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        // Given
        LocalDate invalidStart = LocalDate.of(2025, 12, 1);
        LocalDate invalidEnd = LocalDate.of(2025, 11, 1);

        // When & Then
        assertThatThrownBy(() -> calendarBookingService.getCalendarEntries(invalidStart, invalidEnd))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("fecha de inicio")
            .hasMessageContaining("posterior");

        verify(calendarBookingRepository, never()).findCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("Debe aceptar cuando start y end son la misma fecha")
    void shouldAcceptWhenStartAndEndAreTheSameDate() {
        // Given
        LocalDate sameDate = LocalDate.of(2025, 11, 15);
        when(calendarBookingRepository.findCalendarEntries(sameDate, sameDate))
            .thenReturn(Collections.emptyList());

        // When
        List<CalendarEntryDTO> result = calendarBookingService.getCalendarEntries(sameDate, sameDate);

        // Then
        assertThat(result).isEmpty();
        verify(calendarBookingRepository, times(1)).findCalendarEntries(sameDate, sameDate);
    }

    @Test
    @DisplayName("Debe manejar fechas en diferentes años")
    void shouldHandleDatesInDifferentYears() {
        // Given
        LocalDate start = LocalDate.of(2025, 12, 15);
        LocalDate end = LocalDate.of(2026, 1, 15);

        CalendarEntryDTO entry = new CalendarEntryDTO(
            1L,
            "Test Guest",
            "301",
            LocalDate.of(2025, 12, 20),
            LocalDate.of(2026, 1, 5)
        );

        when(calendarBookingRepository.findCalendarEntries(start, end))
            .thenReturn(List.of(entry));

        // When
        List<CalendarEntryDTO> result = calendarBookingService.getCalendarEntries(start, end);

        // Then
        assertThat(result)
            .hasSize(1)
            .containsExactly(entry);
        verify(calendarBookingRepository, times(1)).findCalendarEntries(start, end);
    }
}

