package com.hotelsa.backend.guest.mapper;

import com.hotelsa.backend.guest.dto.GuestRequestDTO;
import com.hotelsa.backend.guest.dto.GuestResponseDTO;
import com.hotelsa.backend.guest.model.Guest;
import org.springframework.stereotype.Component;

@Component
public class GuestMapper {

    // Mapea un DTO de request a una entidad Guest (sin gestionar relaciones)
    public Guest fromRequestDto(GuestRequestDTO dto) {
        Guest guest = new Guest();
        guest.setFullName(dto.getFullName());
        guest.setDocumentType(dto.getDocumentType());
        guest.setDocumentNumber(dto.getDocumentNumber());
        guest.setEmail(dto.getEmail());
        guest.setPhone(dto.getPhone());
        guest.setAddress(dto.getAddress());
        guest.setPreviousCancellations(dto.getPreviousCancellations());
        guest.setTotalBookingsClient(dto.getTotalBookingsClient());
        // hotel se setea en el servicio
        return guest;
    }

    // Mapea una entidad Guest a un DTO de respuesta
    public GuestResponseDTO fromEntity(Guest guest) {
        return GuestResponseDTO.builder()
                .id(guest.getId())
                .fullName(guest.getFullName())
                .documentType(guest.getDocumentType())
                .documentNumber(guest.getDocumentNumber())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .address(guest.getAddress())
                .previousCancellations(guest.getPreviousCancellations())
                .totalBookingsClient(guest.getTotalBookingsClient())
                .hotelId(guest.getHotelId())
                .hotelName(guest.getHotelId() != null ? guest.getHotel().getName() : null)
                .build();
    }
}

