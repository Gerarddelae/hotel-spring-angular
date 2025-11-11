package com.hotelsa.backend.room.service;

import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.exception.RoomNotFoundException;
import com.hotelsa.backend.room.mapper.RoomMapper;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomMapper roomMapper;
    @Mock private AuthService authService;

    @InjectMocks private RoomService roomService;

    private Hotel hotel;
    private Room room;
    private RoomRequestDTO roomRequestDTO;
    private RoomResponseDTO roomResponseDTO;

    @BeforeEach
    void setUp() {
        hotel = new Hotel();
        hotel.setId(100L);
        hotel.setName("Hotel Paradise");

        room = Room.builder()
                .id(1L)
                .hotelId(100L)
                .number("101")
                .type(RoomType.SINGLE)
                .floor(1)
                .capacity(2)
                .pricePerNight(100.0)
                .status(RoomStatus.AVAILABLE)
                .deleted(false)
                .hotel(hotel)
                .build();

        roomRequestDTO = RoomRequestDTO.builder()
                .number("101")
                .type(RoomType.SINGLE)
                .floor(1)
                .capacity(2)
                .pricePerNight(100.0)
                .status(RoomStatus.AVAILABLE)
                .build();

        roomResponseDTO = RoomResponseDTO.builder()
                .id(1L)
                .number("101")
                .type(RoomType.SINGLE)
                .hotelId(100L)
                .hotelName("Hotel Paradise")
                .build();

        // siempre se obtiene el hotelId actual del AuthService
        when(authService.getCurrentHotelId()).thenReturn(100L);
    }

    // ---------------------- CREATE ----------------------
    @Test
    void createRoom_ShouldCreateRoomSuccessfully() {
        when(hotelRepository.findById(100L)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByNumber("101")).thenReturn(false);
        when(roomMapper.fromRequestDto(roomRequestDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.createRoom(roomRequestDTO);

        assertNotNull(result);
        assertEquals("101", result.getNumber());
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void createRoom_ShouldThrow_WhenHotelNotFound() {
        when(hotelRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> roomService.createRoom(roomRequestDTO));
    }

    @Test
    void createRoom_ShouldThrow_WhenNumberExists() {
        when(hotelRepository.findById(100L)).thenReturn(Optional.of(hotel));
        when(roomRepository.existsByNumber("101")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> roomService.createRoom(roomRequestDTO));
    }

    // ---------------------- GET ----------------------
    @Test
    void getRoomById_ShouldReturnRoom_WhenExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("101", result.getNumber());
    }

    @Test
    void getRoomById_ShouldThrow_WhenNotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(999L));
    }

    @Test
    void getRoomsForCurrentHotel_ShouldReturnRoomsList() {
        // findAll() ya es suficiente, el filtro de hotel_id se aplica automáticamente en runtime
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        List<RoomResponseDTO> result = roomService.getRoomsForCurrentHotel();

        assertEquals(1, result.size());
        assertEquals("101", result.get(0).getNumber());
    }

    // ---------------------- UPDATE ----------------------
    @Test
    void updateRoom_ShouldUpdateSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByNumber("101")).thenReturn(false);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.updateRoom(1L, roomRequestDTO);

        assertNotNull(result);
        assertEquals("101", result.getNumber());
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_ShouldThrow_WhenNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(99L, roomRequestDTO));
    }

    @Test
    void updateRoom_ShouldThrow_WhenDuplicateNumber() {
        RoomRequestDTO updateDto = RoomRequestDTO.builder()
                .number("102")
                .type(RoomType.DOUBLE)
                .floor(2)
                .capacity(3)
                .pricePerNight(200.0)
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.existsByNumber("102")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> roomService.updateRoom(1L, updateDto));
    }

    // ---------------------- DELETE ----------------------
    @Test
    void deleteRoom_ShouldSoftDeleteSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        assertTrue(room.isDeleted());
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_ShouldThrow_WhenNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(99L));
    }
}
