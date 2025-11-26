package com.hotelsa.backend.room.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDashboardItemDTO {
    private Long roomId;
    private String number;
    private String status;
    private String roomTypeName;
    private Long currentBookingId; // puede ser null
    private Integer capacity; // nueva propiedad para visualización frontend
}
