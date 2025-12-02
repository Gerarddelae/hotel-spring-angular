package com.hotelsa.backend.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HotelControllerTest {

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelController hotelController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private HotelUpdateRequest updateRequest;
    private HotelResponse hotelResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hotelController).build();
        objectMapper = new ObjectMapper();

        updateRequest = HotelUpdateRequest.builder()
                .name("Hotel Actualizado")
                .address("Nueva Dirección 456")
                .city("Nueva Ciudad")
                .country("Nuevo País")
                .phone("+0987654321")
                .description("Nueva descripción")
                .build();

        hotelResponse = HotelResponse.builder()
                .id(1L)
                .name("Hotel Actualizado")
                .address("Nueva Dirección 456")
                .city("Nueva Ciudad")
                .country("Nuevo País")
                .phone("+0987654321")
                .description("Nueva descripción")
                .build();
    }

    @Test
    @DisplayName("GET /api/hotels/{id} - Obtener hotel exitosamente")
    void getHotelById_Success() throws Exception {
        // Arrange
        when(hotelService.getHotelById(1L)).thenReturn(hotelResponse);

        // Act & Assert
        mockMvc.perform(get("/api/hotels/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Hotel Actualizado"))
                .andExpect(jsonPath("$.address").value("Nueva Dirección 456"))
                .andExpect(jsonPath("$.city").value("Nueva Ciudad"))
                .andExpect(jsonPath("$.country").value("Nuevo País"))
                .andExpect(jsonPath("$.phone").value("+0987654321"))
                .andExpect(jsonPath("$.description").value("Nueva descripción"));

        verify(hotelService).getHotelById(1L);
    }

    @Test
    @DisplayName("GET /api/hotels/{id} - Hotel no encontrado retorna 404")
    void getHotelById_NotFound() throws Exception {
        // Arrange
        when(hotelService.getHotelById(999L)).thenThrow(new HotelNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/hotels/999"))
                .andExpect(status().isNotFound());

        verify(hotelService).getHotelById(999L);
    }

    @Test
    @DisplayName("PUT /api/hotels/{id} - Actualización completa exitosa")
    void updateHotel_Success() throws Exception {
        // Arrange
        when(hotelService.updateHotel(eq(1L), any(HotelUpdateRequest.class))).thenReturn(hotelResponse);

        // Act & Assert
        mockMvc.perform(put("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Hotel Actualizado"))
                .andExpect(jsonPath("$.address").value("Nueva Dirección 456"));

        verify(hotelService).updateHotel(eq(1L), any(HotelUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/hotels/{id} - Hotel no encontrado retorna 404")
    void updateHotel_NotFound() throws Exception {
        // Arrange
        when(hotelService.updateHotel(eq(999L), any(HotelUpdateRequest.class)))
                .thenThrow(new HotelNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(put("/api/hotels/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(hotelService).updateHotel(eq(999L), any(HotelUpdateRequest.class));
    }

    @Test
    @DisplayName("PUT /api/hotels/{id} - Nombre demasiado largo retorna 400")
    void updateHotel_ValidationError_NameTooLong() throws Exception {
        // Arrange
        HotelUpdateRequest invalidRequest = HotelUpdateRequest.builder()
                .name("A".repeat(101)) // Más de 100 caracteres
                .address("Dirección")
                .city("Ciudad")
                .country("País")
                .phone("+1234567890")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(hotelService, never()).updateHotel(any(), any());
    }

    @Test
    @DisplayName("PUT /api/hotels/{id} - Teléfono con formato inválido retorna 400")
    void updateHotel_ValidationError_InvalidPhone() throws Exception {
        // Arrange
        HotelUpdateRequest invalidRequest = HotelUpdateRequest.builder()
                .name("Hotel Test")
                .phone("invalid_phone!@#")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(hotelService, never()).updateHotel(any(), any());
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id} - Actualización parcial exitosa")
    void patchHotel_Success() throws Exception {
        // Arrange
        HotelUpdateRequest partialRequest = HotelUpdateRequest.builder()
                .name("Nuevo Nombre")
                .build();

        when(hotelService.patchHotel(eq(1L), any(HotelUpdateRequest.class))).thenReturn(hotelResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L));

        verify(hotelService).patchHotel(eq(1L), any(HotelUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id} - Hotel no encontrado retorna 404")
    void patchHotel_NotFound() throws Exception {
        // Arrange
        HotelUpdateRequest partialRequest = HotelUpdateRequest.builder()
                .name("Nuevo Nombre")
                .build();

        when(hotelService.patchHotel(eq(999L), any(HotelUpdateRequest.class)))
                .thenThrow(new HotelNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(patch("/api/hotels/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isNotFound());

        verify(hotelService).patchHotel(eq(999L), any(HotelUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id} - Actualización con campos vacíos (body vacío)")
    void patchHotel_EmptyBody() throws Exception {
        // Arrange
        HotelUpdateRequest emptyRequest = HotelUpdateRequest.builder().build();
        when(hotelService.patchHotel(eq(1L), any(HotelUpdateRequest.class))).thenReturn(hotelResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk());

        verify(hotelService).patchHotel(eq(1L), any(HotelUpdateRequest.class));
    }

    @Test
    @DisplayName("PATCH /api/hotels/{id} - Solo actualiza descripción")
    void patchHotel_OnlyDescription() throws Exception {
        // Arrange
        HotelUpdateRequest partialRequest = HotelUpdateRequest.builder()
                .description("Nueva descripción actualizada")
                .build();

        HotelResponse updatedResponse = HotelResponse.builder()
                .id(1L)
                .name("Hotel Original")
                .description("Nueva descripción actualizada")
                .build();

        when(hotelService.patchHotel(eq(1L), any(HotelUpdateRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/hotels/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Nueva descripción actualizada"));

        verify(hotelService).patchHotel(eq(1L), any(HotelUpdateRequest.class));
    }
}
