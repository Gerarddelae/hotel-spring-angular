package com.hotelsa.backend.room.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
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

    /**
     * Activa automáticamente el filtro de soft delete para habitaciones.
     */
    private void activarFiltroSoftDelete() {
        if (entityManager == null) return; // evita NPE en tests

        try {
            Session session = entityManager.unwrap(Session.class);
            if (session != null) {
                Filter filter = session.enableFilter("roomDeletedFilter");
                filter.setParameter("isDeleted", false);
            }
        } catch (Exception e) {
            // Contexto de test/mock donde no hay sesión real
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO createRoom(RoomRequestDTO dto) {
        // Obtener el usuario actual
        User currentUser = getCurrentUser();

        // Cargar explícitamente el hotel desde el repositorio
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        // Validar que el usuario pertenece a ese hotel
        if (!currentUser.getHotel().getId().equals(hotel.getId())) {
            throw new AccessDeniedException("No puedes crear habitaciones para otro hotel");
        }

        // Crear la entidad Room desde el DTO
        Room room = roomMapper.fromRequestDto(dto);
        room.setHotel(hotel); // asignar hotel ya inicializado

        // Guardar en la base de datos
        Room savedRoom = roomRepository.save(room);

        // Mapear a DTO de respuesta
        return roomMapper.fromEntity(savedRoom);
    }


    @Transactional(readOnly = true)
    public RoomResponseDTO getRoomById(Long roomId) {
        activarFiltroSoftDelete();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        return roomMapper.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponseDTO> getRoomsByHotelId(Long hotelId) {
        activarFiltroSoftDelete();
        List<Room> rooms = roomRepository.findByHotel_Id(hotelId);
        return rooms.stream().map(roomMapper::fromEntity).toList();
    }

    @AdminOnly
    @Transactional
    public RoomResponseDTO updateRoom(Long roomId, RoomRequestDTO dto) {
        activarFiltroSoftDelete();
        User currentUser = getCurrentUser();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        if (!room.getHotel().getId().equals(currentUser.getHotel().getId())) {
            throw new AccessDeniedException("Cannot modify rooms from another hotel");
        }

        // Mapear cambios (sin tocar relaciones)
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
        User currentUser = getCurrentUser();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        if (!room.getHotel().getId().equals(currentUser.getHotel().getId())) {
            throw new AccessDeniedException("Cannot delete rooms from another hotel");
        }

        room.setDeleted(true);
        roomRepository.save(room);
    }
}
