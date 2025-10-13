package com.hotelsa.backend.room.service;

import com.hotelsa.backend.auth.service.AuthService;
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
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomMapper roomMapper;
    @Mock private AuthService authService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

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

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setHotel(hotel);
        hotel.addUser(user);

        room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setHotel(hotel);
        room.setPricePerNight(100.0);

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

        lenient().when(authentication.getPrincipal()).thenReturn(user);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authService.getCurrentHotelId()).thenReturn(hotel.getId());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------ CREATE ------------------------
    @Test
    void createRoom_ShouldCreateRoomSuccessfully() {
        when(hotelRepository.findById(hotel.getId())).thenReturn(Optional.of(hotel));
        when(roomMapper.fromRequestDto(roomRequestDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.createRoom(roomRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("101", result.getNumber());
        verify(roomRepository).save(room);
    }

    // ------------------------ GET ------------------------
    @Test
    void getRoomById_ShouldReturnRoom_WhenExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.getRoomById(1L);

        assertEquals(1L, result.getId());
        assertEquals("101", result.getNumber());
    }

    @Test
    void getRoomById_ShouldThrowNotFound_WhenDoesNotExist() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(999L));
    }

    @Test
    void getRoomsForCurrentHotel_ShouldReturnRoomList() {
        List<Room> rooms = List.of(room);
        when(roomRepository.findByHotel_Id(hotel.getId())).thenReturn(rooms);
        when(roomMapper.fromEntity(any(Room.class))).thenReturn(roomResponseDTO);

        List<RoomResponseDTO> result = roomService.getRoomsForCurrentHotel();

        assertEquals(1, result.size());
        assertEquals("101", result.get(0).getNumber());
    }

    // ------------------------ UPDATE ------------------------
    @Test
    void updateRoom_ShouldUpdateRoomSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomRequestDTO updateDTO = RoomRequestDTO.builder()
                .number("102")
                .type(RoomType.DOUBLE)
                .floor(2)
                .capacity(3)
                .pricePerNight(150.0)
                .status(RoomStatus.AVAILABLE)
                .build();

        RoomResponseDTO result = roomService.updateRoom(1L, updateDTO);

        assertEquals("102", room.getNumber());
        verify(roomRepository).save(room);
        assertEquals(1L, result.getId());
    }

    @Test
    void updateRoom_ShouldThrowAccessDenied_WhenOtherHotel() {
        Hotel otherHotel = new Hotel();
        otherHotel.setId(999L);
        room.setHotel(otherHotel);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(AccessDeniedException.class, () -> roomService.updateRoom(1L, roomRequestDTO));
    }

    // ------------------------ DELETE ------------------------
    @Test
    void deleteRoom_ShouldSoftDeleteSuccessfully() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        roomService.deleteRoom(1L);

        assertTrue(room.isDeleted());
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_ShouldThrowAccessDenied_WhenOtherHotel() {
        Hotel otherHotel = new Hotel();
        otherHotel.setId(999L);
        room.setHotel(otherHotel);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(AccessDeniedException.class, () -> roomService.deleteRoom(1L));
    }
}
