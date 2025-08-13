package com.hotelsa.backend.auth.service;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_debeRetornarToken() {
        // Arrange
        AuthRequest request = new AuthRequest("testuser", "123456");

        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .role(Role.USER)
                .build();

        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("encoded")
                .roles("USER")
                .build();

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(mockUserDetails, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateToken(mockUser)).thenReturn("mock-jwt");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt", response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(mockUser);
    }

    @Test
    void register_debeGuardarUsuarioCuandoNoExiste() {
        // Arrange
        RegisterRequest request = new RegisterRequest("newuser","123456", "new@example.com", "USER");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-pass");

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser")
                        && user.getEmail().equals("new@example.com")
                        && user.getPassword().equals("encoded-pass")
                        && user.getRole() == Role.USER
        ));
    }

    @Test
    void register_debeLanzarExcepcionSiUsuarioExiste() {
        // Arrange
        RegisterRequest request = new RegisterRequest("existinguser", "email@example.com", "123456", "USER");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(request)
        );

        assertEquals("El usuario ya existe", ex.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
    }
}
