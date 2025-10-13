package com.hotelsa.backend.room.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.exception.RoomNotFoundException;
import com.hotelsa.backend.room.mapper.RoomMapper;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.repository.RoomRepository;
import com.hotelsa.backend.user.model.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final EntityManager entityManager;
    private final AuthService authService;

    /**
     * Activa automáticamente el filtro de soft delete para habitaciones.
     */
    private void activarFiltroSoftDelete() {
        if (entityManager == null) return;

        try {
            Session session = entityManager.unwrap(Session.class);
            if (session != null) {
                Filter filter = session.enableFilter("roomDeletedFilter");
                filter.setParameter("isDeleted", false);
            }
        } catch (Exception ignored) {
            // En contexto de test/mock sin sesión real
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("Usuario no autenticado");
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO dto) {
        Long hotelId = authService.getCurrentHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        // Verificar que no exista una habitación con el mismo número
        if (roomRepository.existsByNumberAndHotel_Id(dto.getNumber(), hotelId)) {
            throw new IllegalArgumentException("Ya existe una habitación con ese número en el hotel");
        }

        Room room = roomMapper.fromRequestDto(dto);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        return roomMapper.fromEntity(savedRoom);
    }

    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long roomId) {
        activarFiltroSoftDelete();
        Long hotelId = authService.getCurrentHotelId();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada"));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AccessDeniedException("No puedes acceder a habitaciones de otro hotel");
        }

        return roomMapper.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsForCurrentHotel() {
        activarFiltroSoftDelete();
        Long hotelId = authService.getCurrentHotelId();

        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        return rooms.stream().map(roomMapper::fromEntity).toList();
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomRequestDTO dto) {
        activarFiltroSoftDelete();
        Long hotelId = authService.getCurrentHotelId();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada"));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AccessDeniedException("No puedes modificar habitaciones de otro hotel");
        }

        // Actualizar campos
        room.setNumber(dto.getNumber());
        room.setType(dto.getType());
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        room.setStatus(dto.getStatus());

        Room updatedRoom = roomRepository.save(room);
        return roomMapper.fromEntity(updatedRoom);
    }

    @AdminOnly
    @Transactional
    public void deleteRoom(Long roomId) {
        activarFiltroSoftDelete();
        Long hotelId = authService.getCurrentHotelId();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Habitación no encontrada"));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new AccessDeniedException("No puedes eliminar habitaciones de otro hotel");
        }

        room.setDeleted(true);
        roomRepository.save(room);
    }
}
