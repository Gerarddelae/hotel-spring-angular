package com.hotelsa.backend.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String description;
}
