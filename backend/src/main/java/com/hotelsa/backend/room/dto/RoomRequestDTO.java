package com.hotelsa.backend.room.dto;

import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequestDTO {

    @NotBlank(message = "El número de habitación no puede estar vacío")
    private String number;

    @NotNull(message = "El tipo de habitación es obligatorio")
    private RoomType type;

    @NotNull(message = "El piso es obligatorio")
    @Min(value = 0, message = "El piso no puede ser negativo")
    private Integer floor;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private Integer capacity;

    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio por noche debe ser mayor que 0")
    private Double pricePerNight;

    @NotNull(message = "El estado de la habitación es obligatorio")
    private RoomStatus status;

//    @NotNull(message = "El hotelId es obligatorio")
//    private Long hotelId;
}
