package com.hotelsa.backend.user.controller;

import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.auth.JwtFilter;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad para el test
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // Mockeamos los beans que requiere JwtFilter para que no rompa el contexto
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtFilter jwtFilter;

    @Test
    void registerEmployee_debeRetornarUsuarioCreado() throws Exception {
        // Arrange
        UserDto mockUserDto = UserDto.builder()
                .username("employee1")
                .email("employee@mail.com")
                .role(Role.USER)
                .build();

        Mockito.when(userService.registerEmployee(any(RegisterUserDto.class)))
                .thenReturn(mockUserDto);

        String requestJson = """
            {
                "username": "employee1",
                "email": "employee@mail.com",
                "password": "password123"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/users/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("employee1"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void registerEmployee_debeRetornarBadRequestCuandoFaltanCampos() throws Exception {
        String requestJson = """
            {
                "email": "employee@mail.com"
            }
            """;

        mockMvc.perform(post("/users/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
