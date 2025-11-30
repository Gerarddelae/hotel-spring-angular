package com.hotelsa.backend.booking.controller;

import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.booking.dto.CalendarEntryDTO;
import com.hotelsa.backend.booking.service.CalendarBookingService;
import com.hotelsa.backend.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para CalendarBookingController.
 * Verifica los endpoints REST y el manejo de errores HTTP.
 */
@WebMvcTest(CalendarBookingController.class)
@DisplayName("CalendarBookingController Integration Tests")
class CalendarBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarBookingService calendarBookingService;

    @MockBean
    private JwtService jwtService; // Necesario para el contexto de seguridad

    private List<CalendarEntryDTO> mockEntries;

    @BeforeEach
    void setUp() {
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

        mockEntries = Arrays.asList(entry1, entry2);
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 200 con entradas del calendario")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn200WithCalendarEntries() throws Exception {
        // Given
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2025, 11, 30);

        when(calendarBookingService.getCalendarEntries(start, end))
            .thenReturn(mockEntries);

        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-01")
                .param("end", "2025-11-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].bookingId").value(1))
            .andExpect(jsonPath("$[0].guestName").value("John Doe"))
            .andExpect(jsonPath("$[0].roomNumber").value("101"))
            .andExpect(jsonPath("$[0].checkInDate").value("2025-11-05"))
            .andExpect(jsonPath("$[0].checkOutDate").value("2025-11-10"))
            .andExpect(jsonPath("$[1].bookingId").value(2))
            .andExpect(jsonPath("$[1].guestName").value("Jane Smith"))
            .andExpect(jsonPath("$[1].roomNumber").value("203"));

        verify(calendarBookingService, times(1)).getCalendarEntries(start, end);
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 200 con lista vacía cuando no hay entradas")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn200WithEmptyListWhenNoEntries() throws Exception {
        // Given
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2025, 11, 30);

        when(calendarBookingService.getCalendarEntries(start, end))
            .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-01")
                .param("end", "2025-11-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));

        verify(calendarBookingService, times(1)).getCalendarEntries(start, end);
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 500 cuando falta parámetro start")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn500WhenStartParamIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("end", "2025-11-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(calendarBookingService, never()).getCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 500 cuando falta parámetro end")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn500WhenEndParamIsMissing() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-01")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(calendarBookingService, never()).getCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 401 cuando no está autenticado")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-01")
                .param("end", "2025-11-30")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verify(calendarBookingService, never()).getCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe permitir acceso con rol EMPLOYEE")
    @WithMockUser(roles = "EMPLOYEE")
    void shouldAllowAccessWithEmployeeRole() throws Exception {
        // Given
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2025, 11, 30);

        when(calendarBookingService.getCalendarEntries(start, end))
            .thenReturn(mockEntries);

        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-01")
                .param("end", "2025-11-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));

        verify(calendarBookingService, times(1)).getCalendarEntries(start, end);
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe retornar 500 con formato de fecha inválido")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn500WithInvalidDateFormat() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "invalid-date")
                .param("end", "2025-11-30")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

        verify(calendarBookingService, never()).getCalendarEntries(any(), any());
    }

    @Test
    @DisplayName("GET /api/calendar/entries - Debe manejar el mismo día para start y end")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleSameDateForStartAndEnd() throws Exception {
        // Given
        LocalDate sameDate = LocalDate.of(2025, 11, 15);

        when(calendarBookingService.getCalendarEntries(sameDate, sameDate))
            .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/calendar/entries")
                .param("start", "2025-11-15")
                .param("end", "2025-11-15")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(calendarBookingService, times(1)).getCalendarEntries(sameDate, sameDate);
    }
}

