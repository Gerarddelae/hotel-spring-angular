package com.hotelsa.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private String email;

    // Datos del hotel
    private String hotelName;
    private String address;
    private String city;
    private String country;
    private String phone;
    private String hotelEmail;
    private String description;
}

