package com.hotelsa.backend.guest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.guest.dto.GuestRequestDTO;
import com.hotelsa.backend.guest.dto.GuestResponseDTO;
import com.hotelsa.backend.guest.service.GuestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GuestController.class)
@AutoConfigureMockMvc(addFilters = false)
class GuestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestService guestService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private GuestResponseDTO guestResponseDTO;
    private GuestRequestDTO guestRequestDTO;

    @BeforeEach
    void setUp() {
        guestResponseDTO = GuestResponseDTO.builder()
                .id(1L)
                .fullName("John Doe")
                .documentType("DNI")
                .documentNumber("123456789")
                .email("johndoe@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .previousCancellations(0)
                .totalBookingsClient(5)
                .hotelId(1L)
                .hotelName("Hotel Test")
                .build();

        guestRequestDTO = GuestRequestDTO.builder()
                .fullName("John Doe")
                .documentType("DNI")
                .documentNumber("123456789")
                .email("johndoe@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .previousCancellations(0)
                .totalBookingsClient(5)
                .build();
    }

    @Test
    void shouldCreateGuestSuccessfully() throws Exception {
        when(guestService.createGuest(any(GuestRequestDTO.class))).thenReturn(guestResponseDTO);

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"));

        verify(guestService, times(1)).createGuest(any(GuestRequestDTO.class));
    }

    @Test
    void shouldGetGuestById() throws Exception {
        when(guestService.getGuestById(1L)).thenReturn(guestResponseDTO);

        mockMvc.perform(get("/guests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(guestService, times(1)).getGuestById(1L);
    }

    @Test
    void shouldListGuestsForCurrentHotel() throws Exception {
        List<GuestResponseDTO> guests = List.of(guestResponseDTO);
        when(guestService.getGuestsForCurrentHotel()).thenReturn(guests);

        mockMvc.perform(get("/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(guestService, times(1)).getGuestsForCurrentHotel();
    }

    @Test
    void shouldSearchGuestsByName() throws Exception {
        List<GuestResponseDTO> guests = List.of(guestResponseDTO);
        when(guestService.searchGuests("John")).thenReturn(guests);

        mockMvc.perform(get("/guests/search").param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));

        verify(guestService, times(1)).searchGuests("John");
    }

    @Test
    void shouldGetGuestByEmail() throws Exception {
        when(guestService.getGuestByEmail("johndoe@example.com")).thenReturn(guestResponseDTO);

        mockMvc.perform(get("/guests/email/{email}", "johndoe@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("johndoe@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(guestService, times(1)).getGuestByEmail("johndoe@example.com");
    }

    @Test
    void shouldUpdateGuestSuccessfully() throws Exception {
        GuestRequestDTO updateDTO = GuestRequestDTO.builder()
                .fullName("John Doe Updated")
                .documentType("DNI")
                .documentNumber("123456789")
                .email("johndoe_updated@example.com")
                .phone("1234567890")
                .address("123 Main St")
                .previousCancellations(0)
                .totalBookingsClient(6)
                .build();

        GuestResponseDTO updatedResponse = GuestResponseDTO.builder()
                .id(1L)
                .fullName("John Doe Updated")
                .email("johndoe_updated@example.com")
                .hotelId(1L)
                .hotelName("Hotel Test")
                .build();

        when(guestService.updateGuest(eq(1L), any(GuestRequestDTO.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/guests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("John Doe Updated"))
                .andExpect(jsonPath("$.email").value("johndoe_updated@example.com"));

        verify(guestService, times(1)).updateGuest(eq(1L), any(GuestRequestDTO.class));
    }

    @Test
    void shouldDeleteGuestSuccessfully() throws Exception {
        doNothing().when(guestService).deleteGuest(1L);

        mockMvc.perform(delete("/guests/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(guestService, times(1)).deleteGuest(1L);
    }
}
