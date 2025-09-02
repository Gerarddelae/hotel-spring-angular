package com.hotelsa.backend.room.service;

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
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomMapper roomMapper;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private RoomService roomService;

    private Hotel hotel;
    private Room room;
    private RoomRequestDTO roomRequestDTO;
    private RoomResponseDTO roomResponseDTO;

    @BeforeEach
    void setUp() {
        // Crear hotel y asignar usuario
        hotel = new Hotel();
        hotel.setId(100L);
        hotel.setName("Hotel Paradise");

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("admin");
        currentUser.setHotel(hotel);

        hotel.addUser(currentUser);

        // Crear habitación
        room = new Room();
        room.setId(1L);
        room.setNumber("101");
        room.setHotel(hotel);
        room.setPricePerNight(100.0);

        roomRequestDTO = new RoomRequestDTO();
        roomRequestDTO.setHotelId(hotel.getId());
        roomRequestDTO.setNumber("101");
        roomRequestDTO.setType(RoomType.SINGLE);
        roomRequestDTO.setFloor(1);
        roomRequestDTO.setCapacity(2);
        roomRequestDTO.setPricePerNight(100.0);
        roomRequestDTO.setStatus(RoomStatus.AVAILABLE);

        roomResponseDTO = new RoomResponseDTO();
        roomResponseDTO.setId(1L);
        roomResponseDTO.setNumber("101");

        mockAuthenticatedUser(currentUser);
    }

    private void mockAuthenticatedUser(User user) {
        // los stubs seguirán funcionando, solo que Mockito no se quejará si no se usan
        lenient().when(authentication.getPrincipal()).thenReturn(user);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }


    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------ CREATE ------------------------
    @Test
    void createRoom_ShouldCreateRoom_WhenValidHotel() {
        // Mockear hotel
        Hotel hotel = new Hotel();
        hotel.setId(roomRequestDTO.getHotelId());
        hotel.setName("Hotel Test");

        // Mockear que el usuario actual está autenticado (si tu método getCurrentUser lo requiere)
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setHotel(hotel); // asignar hotel
        // Suponiendo que tienes un método de utilidad para mockear usuario autenticado
        mockAuthenticatedUser(currentUser);

        // Mockear comportamiento de los mappers y repositorio
        when(hotelRepository.findById(roomRequestDTO.getHotelId())).thenReturn(Optional.of(hotel));
        when(roomMapper.fromRequestDto(roomRequestDTO)).thenReturn(room);
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        // Ejecutar método
        RoomResponseDTO result = roomService.createRoom(roomRequestDTO);

        // Verificaciones
        assertNotNull(result);
        assertEquals(roomResponseDTO.getId(), result.getId());
        verify(roomRepository).save(room);
        verify(hotelRepository).findById(roomRequestDTO.getHotelId());
    }


    @Test
    void createRoom_ShouldThrowAccessDenied_WhenOtherHotel() {
        // Usuario autenticado
        Hotel userHotel = new Hotel();
        userHotel.setId(1L); // hotel del usuario
        User currentUser = new User();
        currentUser.setHotel(userHotel);
        mockAuthenticatedUser(currentUser);

        // DTO con hotel diferente
        RoomRequestDTO dtoOtherHotel = new RoomRequestDTO();
        dtoOtherHotel.setHotelId(999L);

        // Mock del hotel solicitado
        Hotel otherHotel = new Hotel();
        otherHotel.setId(999L);
        when(hotelRepository.findById(999L)).thenReturn(Optional.of(otherHotel));

        // Debe lanzar AccessDeniedException
        assertThrows(AccessDeniedException.class, () -> roomService.createRoom(dtoOtherHotel));
    }

    @Test
    void createRoom_ShouldThrowHotelNotFound_WhenHotelDoesNotExist() {
        // Usuario autenticado
        Hotel userHotel = new Hotel();
        userHotel.setId(1L);
        User currentUser = new User();
        currentUser.setHotel(userHotel);
        mockAuthenticatedUser(currentUser);

        // DTO con hotel inexistente
        RoomRequestDTO dto = new RoomRequestDTO();
        dto.setHotelId(999L);

        // No hay mock para el hotel => Optional.empty()
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> roomService.createRoom(dto));
    }
    // ------------------------ GET ------------------------
    @Test
    void getRoomById_ShouldReturnRoom_WhenExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomResponseDTO result = roomService.getRoomById(1L);

        assertEquals(roomResponseDTO.getId(), result.getId());
    }

    @Test
    void getRoomById_ShouldThrowNotFound_WhenRoomDoesNotExist() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(999L));
    }

    @Test
    void getRoomsByHotelId_ShouldReturnRoomsList() {
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findByHotel_Id(hotel.getId())).thenReturn(rooms);
        when(roomMapper.fromEntity(any(Room.class))).thenReturn(roomResponseDTO);

        List<RoomResponseDTO> result = roomService.getRoomsByHotelId(hotel.getId());

        assertEquals(1, result.size());
    }

    // ------------------------ UPDATE ------------------------
    @Test
    void updateRoom_ShouldUpdateRoom_WhenValidHotel() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.fromEntity(room)).thenReturn(roomResponseDTO);

        RoomRequestDTO updateDto = new RoomRequestDTO();
        updateDto.setNumber("102");
        updateDto.setType(RoomType.DOUBLE);
        updateDto.setFloor(1);
        updateDto.setCapacity(2);
        updateDto.setPricePerNight(150.0);
        updateDto.setStatus(RoomStatus.AVAILABLE);

        RoomResponseDTO result = roomService.updateRoom(1L, updateDto);

        assertEquals("102", room.getNumber());
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoom_ShouldThrowAccessDenied_WhenOtherHotel() {
        RoomRequestDTO updateDto = new RoomRequestDTO();
        Room roomOtherHotel = new Room();
        Hotel otherHotel = new Hotel();
        otherHotel.setId(999L);
        roomOtherHotel.setHotel(otherHotel);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomOtherHotel));

        assertThrows(AccessDeniedException.class, () -> roomService.updateRoom(1L, updateDto));
    }

    // ------------------------ DELETE ------------------------
    @Test
    void deleteRoom_ShouldSoftDelete_WhenValidHotel() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        roomService.deleteRoom(1L);

        assertTrue(room.isDeleted());
        verify(roomRepository).save(room);
    }

    @Test
    void deleteRoom_ShouldThrowAccessDenied_WhenOtherHotel() {
        Room roomOtherHotel = new Room();
        Hotel otherHotel = new Hotel();
        otherHotel.setId(999L);
        roomOtherHotel.setHotel(otherHotel);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(roomOtherHotel));

        assertThrows(AccessDeniedException.class, () -> roomService.deleteRoom(1L));
    }
}
