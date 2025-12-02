package com.hotelsa.backend.hotel.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelUpdateRequest {

    @Size(min = 1, max = 100, message = "El nombre debe tener entre 1 y 100 caracteres")
    private String name;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String address;

    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    private String city;

    @Size(max = 100, message = "El país no puede exceder 100 caracteres")
    private String country;

    @Pattern(regexp = "^[+]?[0-9\\s\\-()]*$", message = "Formato de teléfono inválido")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String phone;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;
}
