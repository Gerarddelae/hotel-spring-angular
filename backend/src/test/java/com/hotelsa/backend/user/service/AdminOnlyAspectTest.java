package com.hotelsa.backend.user.service;

import com.hotelsa.backend.aop.AdminOnlyAspect;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class AdminOnlyAspectTest {

    private UserService proxyService;

    @BeforeEach
    void setUp() {
        // Servicio simulado
        UserService targetService = mock(UserService.class);

        // Aspecto
        AdminOnlyAspect aspect = new AdminOnlyAspect();

        // Crear proxy con aspecto
        AspectJProxyFactory factory = new AspectJProxyFactory(targetService);
        factory.addAspect(aspect);
        proxyService = factory.getProxy();
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotAdmin() {
        // Arrange: usuario normal
        User normalUser = User.builder()
                .role(Role.USER)
                .build();

        RegisterUserDto dto = RegisterUserDto.builder()
                .username("employee1")
                .email("employee@mail.com")
                .password("password123")
                .build();

        // Simular usuario en SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(normalUser, null, normalUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert
        assertThrows(AccessDeniedException.class,
                () -> proxyService.registerEmployee(dto));

        // Limpiar SecurityContext
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotThrowExceptionWhenUserIsAdmin() {
        // Arrange: admin con hotel
        Hotel hotel = Hotel.builder()
                .id(1L)
                .name("Hotel Test")
                .build();

        User adminUser = User.builder()
                .role(Role.ADMIN)
                .hotel(hotel)
                .build();

        RegisterUserDto dto = RegisterUserDto.builder()
                .username("employee1")
                .email("employee@mail.com")
                .password("password123")
                .build();

        // Simular usuario admin en SecurityContext
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Act & Assert: no debe lanzar excepción
        assertDoesNotThrow(() -> proxyService.registerEmployee(dto));

        // Limpiar SecurityContext
        SecurityContextHolder.clearContext();
    }
}
