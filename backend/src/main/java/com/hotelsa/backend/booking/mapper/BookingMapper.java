package com.hotelsa.backend.booking.mapper;

import com.hotelsa.backend.booking.dto.BookingRequestDTO;
import com.hotelsa.backend.booking.dto.BookingResponseDTO;
import com.hotelsa.backend.booking.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

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

    // Mapea una entidad Booking a un DTO de respuesta
    public BookingResponseDTO fromEntity(Booking booking) {
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
                .build();
    }
}

