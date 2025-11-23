package com.hotelsa.backend.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para representar un addon en el contexto de una reserva.
 * Incluye información de cantidad y subtotal calculado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookingAddonResponse {

    private Long id;
    private String name;
    private String description;
    private Integer price;
    private LocalDateTime createdAt;

    // Campos específicos del contexto de reserva
    private Integer quantity;
    private Integer subtotal; // price * quantity
}

