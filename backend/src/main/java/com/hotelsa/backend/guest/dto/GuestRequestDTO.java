package com.hotelsa.backend.guest.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestRequestDTO {

    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(max = 50, message = "El nombre completo no puede exceder 50 caracteres")
    private String fullName;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no puede exceder 50 caracteres")
    private String documentType;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 50, message = "El número de documento no puede exceder 50 caracteres")
    private String documentNumber;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    @Size(max = 100, message = "El correo electrónico no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String phone;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String address;

    @NotNull(message = "Las cancelaciones previas son obligatorias")
    @Min(value = 0, message = "Las cancelaciones previas no pueden ser negativas")
    private Integer previousCancellations;

    @NotNull(message = "El total de reservas del cliente es obligatorio")
    @Min(value = 0, message = "El total de reservas no puede ser negativo")
    private Integer totalBookingsClient;
}

