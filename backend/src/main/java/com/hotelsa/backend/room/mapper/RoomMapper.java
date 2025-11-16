package com.hotelsa.backend.room.mapper;

import com.hotelsa.backend.room.dto.RoomRequestDTO;
import com.hotelsa.backend.room.dto.RoomResponseDTO;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.model.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    // Mapea un DTO de request a una entidad Room (sin gestionar relaciones)
    public Room fromRequestDto(RoomRequestDTO dto) {
        Room room = new Room();
        room.setNumber(dto.getNumber());
        room.setType(dto.getType());
        room.setFloor(dto.getFloor());
        room.setCapacity(dto.getCapacity());
        room.setPricePerNight(dto.getPricePerNight());
        room.setStatus(dto.getStatus());
        // hotel se setea en el servicio
        return room;
    }

    // Mapea una entidad Room a un DTO de respuesta
    public RoomResponseDTO fromEntity(Room room) {
        return RoomResponseDTO.builder()
                .id(room.getId())
                .number(room.getNumber())
                .type(room.getType())
                .floor(room.getFloor())
                .capacity(room.getCapacity())
                .pricePerNight(room.getPricePerNight())
                .status(room.getStatus())
                .hotelId(room.getHotelId())
                .hotelName(room.getHotelId() != null ? room.getHotel().getName() : null)
                .build();
    }
}
