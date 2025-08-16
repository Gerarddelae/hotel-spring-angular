package com.hotelsa.backend.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String description;

    private List<Long> userIds; // lista de IDs de usuarios asociados
}
