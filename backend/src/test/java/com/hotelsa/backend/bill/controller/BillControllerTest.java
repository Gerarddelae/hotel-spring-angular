package com.hotelsa.backend.bill.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.auth.JwtFilter;
import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.enums.BillStatus;
import com.hotelsa.backend.bill.enums.PaymentMethod;
import com.hotelsa.backend.bill.exception.BillNotFoundException;
import com.hotelsa.backend.bill.service.BillService;
import com.hotelsa.backend.booking.exception.BookingNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
@AutoConfigureMockMvc(addFilters = false)
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillService billService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private BillRequestDTO requestDto;
    private BillResponseDTO responseDto;

    @BeforeEach
    void setUp() {
        requestDto = BillRequestDTO.builder()
                .notes("Notas de prueba")
                .status(BillStatus.UNPAID)
                .build();

        responseDto = BillResponseDTO.builder()
                .id(100L)
                .bookingId(5L)
                .notes("Notas de prueba")
                .totalAmount(BigDecimal.valueOf(120))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBill_debeRetornarCreatedCuandoDtoValido() throws Exception {
        when(billService.createBill(any(Long.class), any(BillRequestDTO.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/bills/{bookingId}", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.bookingId").value(5));
    }

    @Test
    void createBill_debeRetornarNotFoundCuandoBookingNoExiste() throws Exception {
        when(billService.createBill(any(Long.class), any(BillRequestDTO.class)))
                .thenThrow(new BookingNotFoundException("Booking not found"));

        mockMvc.perform(post("/api/bills/{bookingId}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_debeRetornarFacturaCuandoExiste() throws Exception {
        when(billService.findById(100L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/bills/{id}", 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.bookingId").value(5));
    }

    @Test
    void getById_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        when(billService.findById(999L)).thenThrow(new BillNotFoundException("Bill not found"));

        mockMvc.perform(get("/api/bills/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_debeRetornarListaDeFacturas() throws Exception {
        BillResponseDTO other = BillResponseDTO.builder().id(101L).totalAmount(BigDecimal.valueOf(50)).build();
        when(billService.findAll()).thenReturn(List.of(responseDto, other));

        mockMvc.perform(get("/api/bills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[1].id").value(101));
    }

    @Test
    void updateStatus_debeRetornarFacturaActualizada() throws Exception {
        when(billService.updateStatus(100L, BillStatus.PAID)).thenReturn(responseDto);

        mockMvc.perform(patch("/api/bills/{id}/status", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BillStatus.PAID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updatePaymentMethod_debeRetornarFacturaActualizada() throws Exception {
        BillResponseDTO updated = BillResponseDTO.builder()
                .id(100L)
                .bookingId(5L)
                .paymentMethod(PaymentMethod.CREDIT_CARD.name())
                .totalAmount(responseDto.getTotalAmount())
                .createdAt(responseDto.getCreatedAt())
                .build();
        when(billService.updatePaymentMethod(100L, PaymentMethod.CREDIT_CARD)).thenReturn(updated);

        mockMvc.perform(patch("/api/bills/{id}/payment-method", 100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"CREDIT_CARD\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.paymentMethod").value("CREDIT_CARD"));
    }

    @Test
    void delete_debeRetornarNoContentCuandoExitoso() throws Exception {
        doNothing().when(billService).delete(100L);

        mockMvc.perform(delete("/api/bills/{id}", 100))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        doThrow(new BillNotFoundException("Not found")).when(billService).delete(999L);

        mockMvc.perform(delete("/api/bills/{id}", 999))
                .andExpect(status().isNotFound());
    }
}
