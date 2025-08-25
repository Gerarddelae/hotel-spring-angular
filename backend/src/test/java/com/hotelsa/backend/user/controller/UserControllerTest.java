package com.hotelsa.backend.user.controller;

import com.hotelsa.backend.auth.service.JwtService;
import com.hotelsa.backend.auth.JwtFilter;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserNotFoundException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void getUsersByHotel_debeRetornarListaDeUsuarios() throws Exception {
        // Arrange
        UserDto user1 = UserDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@mail.com")
                .role(Role.USER)
                .build();

        UserDto user2 = UserDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@mail.com")
                .role(Role.USER)
                .build();

        Mockito.when(userService.getUsersByHotel())
                .thenReturn(List.of(user1, user2));

        // Act & Assert
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUsersByHotel_debeRetornarListaVaciaCuandoNoHayUsuarios() throws Exception {
        // Arrange
        Mockito.when(userService.getUsersByHotel())
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUserById_debeRetornarUsuarioCuandoExiste() throws Exception {
        // Arrange
        Long userId = 5L;
        UserDto userDto = UserDto.builder()
                .id(userId)
                .username("user5")
                .email("user5@mail.com")
                .role(Role.USER)
                .build();

        Mockito.when(userService.getUserById(userId)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("user5"))
                .andExpect(jsonPath("$.email").value("user5@mail.com"));
    }

    @Test
    void getUserById_debeRetornarNotFoundCuandoNoExiste() throws Exception {
        // Arrange
        Long userId = 999L;

        Mockito.when(userService.getUserById(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
