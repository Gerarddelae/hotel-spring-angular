package com.hotelsa.backend.auth.controller;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// auth/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("EMAIL: " + request.getEmail()); // 👈 Haz esto temporalmente
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "Usuario registrado correctamente"));
    }
}

