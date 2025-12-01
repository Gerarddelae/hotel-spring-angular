package com.hotelsa.backend.hotel.mapper;

import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;
import com.hotelsa.backend.hotel.dto.RegisterHotelDto;
import com.hotelsa.backend.hotel.model.Hotel;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    public Hotel fromRegisterDto(RegisterHotelDto dto) {
        Hotel hotel = new Hotel();
        hotel.setName(dto.getName());
        hotel.setAddress(dto.getAddress());
        hotel.setCity(dto.getCity());
        hotel.setCountry(dto.getCountry());
        hotel.setPhone(dto.getPhone());
        hotel.setDescription(dto.getDescription());
        return hotel;
    }

    /**
     * Convierte una entidad Hotel a HotelResponse DTO.
     *
     * @param hotel Entidad Hotel
     * @return HotelResponse DTO
     */
    public HotelResponse toResponse(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .country(hotel.getCountry())
                .phone(hotel.getPhone())
                .description(hotel.getDescription())
                .build();
    }

    /**
     * Convierte un HotelUpdateRequest a una nueva entidad Hotel.
     *
     * @param request HotelUpdateRequest DTO
     * @return Nueva entidad Hotel
     */
    public Hotel toEntity(HotelUpdateRequest request) {
        if (request == null) {
            return null;
        }
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setCountry(request.getCountry());
        hotel.setPhone(request.getPhone());
        hotel.setDescription(request.getDescription());
        return hotel;
    }

    /**
     * Actualiza una entidad Hotel existente con los campos no nulos del request (PATCH).
     *
     * @param request HotelUpdateRequest con campos a actualizar
     * @param hotel   Entidad Hotel a actualizar
     */
    public void updateEntityFromRequest(HotelUpdateRequest request, Hotel hotel) {
        if (request == null || hotel == null) {
            return;
        }
        if (request.getName() != null) {
            hotel.setName(request.getName());
        }
        if (request.getAddress() != null) {
            hotel.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            hotel.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            hotel.setCountry(request.getCountry());
        }
        if (request.getPhone() != null) {
            hotel.setPhone(request.getPhone());
        }
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
    }
}
