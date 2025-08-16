package com.hotelsa.backend.hotel.mapper;

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
}
