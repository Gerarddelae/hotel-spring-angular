package com.hotelsa.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String hotelName;           // antes era hotelId
    private List<String> authorities;   // lista de roles del usuario
}
