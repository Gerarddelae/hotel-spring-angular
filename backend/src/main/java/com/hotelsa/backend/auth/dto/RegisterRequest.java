package com.hotelsa.backend.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String role; // Puede ser "ADMIN", "USER", etc.
}

