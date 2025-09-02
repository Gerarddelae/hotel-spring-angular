package com.hotelsa.backend.room.dto;

import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDTO {
    private Long id;
    private String number;
    private RoomType type;
    private Integer floor;
    private Integer capacity;
    private Double pricePerNight;
    private RoomStatus status;
    private Long hotelId;
    private String hotelName; // opcional, por si queremos devolver el nombre del hotel
}
