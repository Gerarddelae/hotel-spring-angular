package com.hotelsa.backend.room.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.service.RoomService;
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

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ Desactiva filtros de seguridad
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoomResponseDTO roomResponseDTO;

    @BeforeEach
    void setUp() {
        roomResponseDTO = RoomResponseDTO.builder()
                .id(1L)
                .number("101")
                .type(RoomType.SINGLE)
                .floor(1)
                .capacity(2)
                .pricePerNight(100.0)
                .status(RoomStatus.AVAILABLE)
                .hotelName("Hotel Test")
                .build(); // ✅ Eliminado hotelId (ya no se envía en DTO)
    }

    @Test
    void shouldCreateRoomSuccessfully() throws Exception {
        RoomRequestDTO requestDTO = new RoomRequestDTO(
                "101", RoomType.SINGLE, 1, 2, 100.0, RoomStatus.AVAILABLE
        );

        when(roomService.createRoom(any(RoomRequestDTO.class))).thenReturn(roomResponseDTO);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("101"))
                .andExpect(jsonPath("$.type").value("SINGLE"))
                .andExpect(jsonPath("$.hotelName").value("Hotel Test"));
    }

    @Test
    void shouldGetRoomById() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(roomResponseDTO);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.number").value("101"));
    }

    @Test
    void shouldListRoomsForCurrentHotel() throws Exception {
        List<RoomResponseDTO> rooms = List.of(roomResponseDTO);
        when(roomService.getRoomsForCurrentHotel()).thenReturn(rooms);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].number").value("101"));
    }

    @Test
    void shouldUpdateRoomSuccessfully() throws Exception {
        RoomRequestDTO updateDTO = new RoomRequestDTO(
                "102", RoomType.DOUBLE, 1, 3, 150.0, RoomStatus.AVAILABLE
        );

        RoomResponseDTO updatedRoom = RoomResponseDTO.builder()
                .id(1L)
                .number("102")
                .type(RoomType.DOUBLE)
                .floor(1)
                .capacity(3)
                .pricePerNight(150.0)
                .status(RoomStatus.AVAILABLE)
                .hotelName("Hotel Test")
                .build();

        when(roomService.updateRoom(eq(1L), any(RoomRequestDTO.class))).thenReturn(updatedRoom);

        mockMvc.perform(put("/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("102"))
                .andExpect(jsonPath("$.type").value("DOUBLE"));
    }

    @Test
    void shouldDeleteRoomSuccessfully() throws Exception {
        Long roomId = 1L;
        doNothing().when(roomService).deleteRoom(roomId);

        mockMvc.perform(delete("/rooms/{id}", roomId))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).deleteRoom(roomId);
    }
}
