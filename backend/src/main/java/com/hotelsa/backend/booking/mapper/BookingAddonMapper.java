package com.hotelsa.backend.booking.mapper;

import com.hotelsa.backend.addon.model.Addon;
import com.hotelsa.backend.booking.dto.BookingAddonResponse;
import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades BookingAddon a DTOs BookingAddonResponse
 */
@Component
public class BookingAddonMapper {

    /**
     * Mapea una entidad Addon con cantidad a BookingAddonResponse
     */
    public BookingAddonResponse fromAddonWithQuantity(Addon addon, Integer quantity) {
        Integer qty = quantity != null ? quantity : 1;
        Integer price = addon.getPrice() != null ? addon.getPrice() : 0;
        Integer subtotal = price * qty;

        return BookingAddonResponse.builder()
                .id(addon.getId())
                .name(addon.getName())
                .description(addon.getDescription())
                .price(price)
                .createdAt(addon.getCreatedAt())
                .quantity(qty)
                .subtotal(subtotal)
                .build();
    }

    /**
     * Mapea una entidad BookingAddon completa a BookingAddonResponse
     */
    public BookingAddonResponse fromEntity(BookingAddon bookingAddon) {
        return fromAddonWithQuantity(bookingAddon.getAddon(), bookingAddon.getQuantity());
    }
}

