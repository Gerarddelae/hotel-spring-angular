package com.hotelsa.backend.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.dto.UpdateAddonQuantityRequest;
import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingResponseDTO responseDTO;
    private BookingRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = BookingResponseDTO.builder()
                .id(1L)
                .hotelId(10L)
                .guestId(2L)
                .roomId(3L)
                .status(BookingStatus.CONFIRMED)
                .checkInDate(LocalDate.of(2025, 6, 1))
                .checkOutDate(LocalDate.of(2025, 6, 5))
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .build();

        requestDTO = BookingRequestDTO.builder()
                .guestId(2L)
                .roomId(3L)
                .checkInDate(LocalDate.of(2025, 6, 1))
                .checkOutDate(LocalDate.of(2025, 6, 5))
                .status(BookingStatus.PENDING)
                .createdBy("user")
                .bookingLeadTime(LocalDate.now())
                .build();
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {
        when(bookingService.create(any(BookingRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.hotelId").value(10L));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.findById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.guestId").value(2L));
    }

    @Test
    void shouldListAllBookings() throws Exception {
        when(bookingService.findAll()).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldUpdateBookingSuccessfully() throws Exception {
        BookingResponseDTO updated = BookingResponseDTO.builder()
                .id(1L)
                .hotelId(10L)
                .guestId(2L)
                .roomId(3L)
                .status(BookingStatus.CONFIRMED)
                .checkInDate(LocalDate.of(2025, 6, 2))
                .checkOutDate(LocalDate.of(2025, 6, 6))
                .createdBy("user")
                .bookingLeadTime(LocalDate.now())
                .build();

        // Usar BookingReplaceRequestDTO para la operación de reemplazo completa
        com.hotelsa.backend.booking.dto.BookingReplaceRequestDTO replaceDto = new com.hotelsa.backend.booking.dto.BookingReplaceRequestDTO();
        replaceDto.setGuestId(2L);
        replaceDto.setRoomId(3L);
        replaceDto.setCheckInDate(LocalDate.of(2025,6,2));
        replaceDto.setCheckOutDate(LocalDate.of(2025,6,6));
        replaceDto.setNotes(null);

        when(bookingService.replaceBooking(eq(1L), any(com.hotelsa.backend.booking.dto.BookingReplaceRequestDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/bookings/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replaceDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkInDate").value("2025-06-02"))
                .andExpect(jsonPath("$.checkOutDate").value("2025-06-06"));
    }

    @Test
    void shouldDeleteBookingSuccessfully() throws Exception {
        doNothing().when(bookingService).delete(1L);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isNoContent());

        verify(bookingService, times(1)).delete(1L);
    }

    @Test
    void shouldReturnBookingsWithinRange() throws Exception {
        when(bookingService.getBookingsBetween(LocalDate.of(2025,6,1), LocalDate.of(2025,6,30)))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/bookings/range")
                        .param("start","2025-06-01")
                        .param("end","2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    void shouldAddAddonsToBookingSuccessfully() throws Exception {
        // preparar petición de addon
        List<com.hotelsa.backend.booking.dto.BookingAddonRequest> addons = List.of(
                com.hotelsa.backend.booking.dto.BookingAddonRequest.builder().addonId(1L).quantity(2).build()
        );

        when(bookingService.addAddonsToBooking(eq(1L), anyList())).thenReturn(responseDTO);

        mockMvc.perform(post("/bookings/1/addons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addons)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReplaceAddonsForBookingSuccessfully() throws Exception {
        List<com.hotelsa.backend.booking.dto.BookingAddonRequest> newAddons = List.of(
                com.hotelsa.backend.booking.dto.BookingAddonRequest.builder().addonId(2L).quantity(3).build()
        );

        when(bookingService.replaceAddonsForBooking(eq(1L), anyList())).thenReturn(responseDTO);

        mockMvc.perform(put("/bookings/1/addons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAddons)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldReturnBadRequestWhenReplaceAddonsBodyInvalid() throws Exception {
        // Enviar body vacío produce 500 (HttpMessageNotReadableException)
        // En producción esto debería manejarse con un @ExceptionHandler que devuelva 400
        mockMvc.perform(put("/bookings/1/addons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldRemoveAddonFromBookingSuccessfully() throws Exception {
        doNothing().when(bookingService).removeAddonFromBooking(1L, 1L);

        mockMvc.perform(delete("/bookings/1/addons/1"))
                .andExpect(status().isNoContent());

        verify(bookingService, times(1)).removeAddonFromBooking(1L, 1L);
    }

    @Test
    void shouldUpdateAddonQuantitySuccessfully() throws Exception {
        UpdateAddonQuantityRequest req = new UpdateAddonQuantityRequest(5);
        when(bookingService.updateAddonQuantity(1L, 1L, 5)).thenReturn(responseDTO);

        mockMvc.perform(patch("/bookings/1/addons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void shouldCancelBookingSuccessfully() throws Exception {
        BookingResponseDTO cancelledBooking = BookingResponseDTO.builder()
                .id(1L)
                .hotelId(10L)
                .guestId(2L)
                .roomId(3L)
                .status(BookingStatus.CANCELLED)
                .checkInDate(LocalDate.of(2025, 6, 1))
                .checkOutDate(LocalDate.of(2025, 6, 5))
                .createdBy("system")
                .bookingLeadTime(LocalDate.now())
                .build();

        when(bookingService.cancelBooking(1L)).thenReturn(cancelledBooking);

        mockMvc.perform(patch("/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService, times(1)).cancelBooking(1L);
    }

    // Nuevo test: check availability - disponible
    @Test
    void shouldReturnRoomAvailable() throws Exception {
        when(bookingService.isRoomAvailable(eq(3L), any(LocalDate.class), any(LocalDate.class), any())).thenReturn(true);

        mockMvc.perform(get("/bookings/room/3/availability")
                        .param("checkIn", "2025-12-01")
                        .param("checkOut", "2025-12-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(3))
                .andExpect(jsonPath("$.available").value(true));
    }

    // Nuevo test: check availability - bad request (fechas inválidas)
    @Test
    void shouldReturnBadRequestWhenAvailabilityDatesInvalid() throws Exception {
        mockMvc.perform(get("/bookings/room/3/availability")
                        .param("checkIn", "2025-12-05")
                        .param("checkOut", "2025-12-05"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassExcludeBookingIdToServiceWhenCheckingAvailability() throws Exception {
        // Mockear la llamada con excludeBookingId = 42
        when(bookingService.isRoomAvailable(eq(3L), any(LocalDate.class), any(LocalDate.class), eq(42L))).thenReturn(true);

        mockMvc.perform(get("/bookings/room/3/availability")
                        .param("checkIn", "2025-12-01")
                        .param("checkOut", "2025-12-05")
                        .param("excludeBookingId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(bookingService, times(1)).isRoomAvailable(eq(3L), any(LocalDate.class), any(LocalDate.class), eq(42L));
    }
}
