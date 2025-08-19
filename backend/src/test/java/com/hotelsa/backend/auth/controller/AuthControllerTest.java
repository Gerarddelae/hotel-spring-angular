package com.hotelsa.backend.auth.controller;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.auth.service.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void login_debeRetornarAuthResponse() throws Exception {
        // Arrange
        AuthResponse mockResponse = new AuthResponse(
                "mock-token",
                "testuser",
                "Hotel Test",
                List.of("ROLE_ADMIN", "ROLE_USER")
        );
        Mockito.when(authService.login(any(AuthRequest.class))).thenReturn(mockResponse);

        String requestJson = """
            {
                "username": "testuser",
                "password": "123456"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.hotelName").value("Hotel Test"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.authorities[1]").value("ROLE_USER"));
    }

    @Test
    void register_debeRetornarTokenYDatosUsuario() throws Exception {
        // Arrange
        Mockito.when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse(
                        "mock-token",
                        "adminuser",
                        "Hotel Test",
                        List.of("ROLE_ADMIN")
                ));

        String requestJson = """
            {
                "user": {
                    "username": "adminuser",
                    "password": "123456",
                    "email": "admin@example.com"
                },
                "hotel": {
                    "name": "Hotel Test",
                    "address": "Calle Falsa 123",
                    "city": "Ciudad",
                    "country": "Pais",
                    "phone": "123456789",
                    "email": "hotel@example.com",
                    "description": "Hotel de prueba"
                }
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-token"))
                .andExpect(jsonPath("$.hotelName").value("Hotel Test"))
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"));
    }

    @Test
    void register_debeRetornarBadRequestCuandoFaltanCamposEnUser() throws Exception {
        String requestJson = """
            {
                "user": {
                    "email": "admin@example.com"
                },
                "hotel": {
                    "name": "Hotel Test",
                    "address": "Calle Falsa 123",
                    "city": "Ciudad",
                    "country": "Pais",
                    "phone": "123456789",
                    "email": "hotel@example.com",
                    "description": "Hotel de prueba"
                }
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
