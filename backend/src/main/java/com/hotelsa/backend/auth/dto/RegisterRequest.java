package com.hotelsa.backend.auth.dto;

import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.hotel.dto.RegisterHotelDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotNull(message = "Los datos de usuario son obligatorios")
    @Valid
    private RegisterUserDto user;

    @NotNull(message = "Los datos del hotel son obligatorios")
    @Valid
    private RegisterHotelDto hotel;
}