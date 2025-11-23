package com.hotelsa.backend.booking.mapper;

import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.model.Booking;
import com.hotelsa.backend.addon.mapper.AddonMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final AddonMapper addonMapper;

    // Mapea un DTO de request a una entidad Booking (sin gestionar relaciones)
    public Booking fromRequestDto(BookingRequestDTO dto) {
        Booking booking = new Booking();
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setStatus(dto.getStatus());
        booking.setCreatedBy(dto.getCreatedBy());
        booking.setBookingLeadTime(dto.getBookingLeadTime());
        booking.setNotes(dto.getNotes());
        // guest, room y hotel se setean en el servicio
        return booking;
    }

    // Mapea una entidad Booking a un DTO de respuesta (sin addons)
    public BookingResponseDTO fromEntity(Booking booking) {
        return fromEntity(booking, false);
    }

    // Mapea una entidad Booking a un DTO de respuesta con opción de incluir addons
    public BookingResponseDTO fromEntity(Booking booking, boolean includeAddons) {
        return BookingResponseDTO.builder()
                .id(booking.getId())
                .guestId(booking.getGuest() != null ? booking.getGuest().getId() : null)
                .guestName(booking.getGuest() != null ? booking.getGuest().getFullName() : null)
                .roomId(booking.getRoom() != null ? booking.getRoom().getId() : null)
                .roomNumber(booking.getRoom() != null ? booking.getRoom().getNumber() : null)
                .hotelId(booking.getHotelId())
                .hotelName(booking.getHotel() != null ? booking.getHotel().getName() : null)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .createdBy(booking.getCreatedBy())
                .bookingLeadTime(booking.getBookingLeadTime())
                .notes(booking.getNotes())
                .addons(includeAddons && booking.getAddons() != null ?
                    booking.getAddons().stream()
                        .map(ba -> {
                            Integer quantity = ba.getQuantity() != null ? ba.getQuantity() : 1;
                            Integer price = ba.getAddon().getPrice() != null ? ba.getAddon().getPrice() : 0;
                            Integer subtotal = price * quantity;
                            return addonMapper.fromEntityWithQuantity(ba.getAddon(), quantity, subtotal);
                        })
                        .toList() : null)
                .build();
    }
}
