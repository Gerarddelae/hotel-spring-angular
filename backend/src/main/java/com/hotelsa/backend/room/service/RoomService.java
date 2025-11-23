package com.hotelsa.backend.room.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.exception.RoomNotFoundException;
import com.hotelsa.backend.room.mapper.RoomMapper;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import com.hotelsa.backend.booking.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AuthService authService;

    private Long getCurrentHotelId() {
        return authService.getCurrentHotelId();
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO dto) {
        Long hotelId = getCurrentHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        if (roomRepository.existsByNumber(dto.getNumber())) {
            throw new IllegalArgumentException("Ya existe una habitación con ese número en el hotel");
        }

        Room room = roomMapper.fromRequestDto(dto);
        room.setHotel(hotel);
        room.setHotelId(hotelId);

        Room savedRoom = roomRepository.save(room);
        log.debug("✅ Created room {} for hotel {}", savedRoom.getNumber(), hotelId);

        return roomMapper.fromEntity(savedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        return roomMapper.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsForCurrentHotel() {
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().map(roomMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByType(RoomType type) {
        List<Room> rooms = roomRepository.findByType(type);
        return rooms.stream().map(roomMapper::fromEntity).toList();
    }

    // Nuevo: obtiene habitaciones disponibles en el rango dado para el hotel del usuario autenticado
    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        Long hotelId = getCurrentHotelId();
        List<Room> rooms = roomRepository.findAvailableRooms(hotelId, checkIn, checkOut, BookingStatus.CANCELLED);
        return rooms.stream().map(roomMapper::fromEntity).toList();
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomRequestDTO dto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        if (!room.getNumber().equals(dto.getNumber()) &&
                roomRepository.existsByNumber(dto.getNumber())) {
            throw new IllegalArgumentException("Ya existe una habitación con ese número en el hotel");
        }

        room.setNumber(dto.getNumber());
        room.setType(dto.getType());
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        room.setStatus(dto.getStatus());

        Room updatedRoom = roomRepository.save(room);
        log.debug("✅ Updated room {} for hotel {}", updatedRoom.getNumber(), room.getHotelId());

        return roomMapper.fromEntity(updatedRoom);
    }

    @AdminOnly
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada o no pertenece a tu hotel"));

        room.setDeleted(true);
        roomRepository.save(room);
        log.debug("🗑️ Soft deleted room {} for hotel {}", room.getNumber(), room.getHotelId());
    }
}
