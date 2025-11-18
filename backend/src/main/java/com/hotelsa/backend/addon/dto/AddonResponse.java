package com.hotelsa.backend.addon.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AddonResponse {

    private Long id;
    private String name;
    private String description;
    private Integer price;

    // Exponer createdAt si se hace así en otros response DTOs
    private LocalDateTime createdAt;

    // Cantidad asociada cuando el addon forma parte de una reserva (BookingAddon.quantity)
    private Integer quantity;

    // Subtotal calculado: price * quantity
    private Integer subtotal;
}
