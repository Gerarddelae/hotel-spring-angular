package com.hotelsa.backend.auth.service;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private HotelRepository hotelRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_debeRetornarTokenYHotelId() {
        // Arrange
        AuthRequest request = new AuthRequest("testuser", "123456");

        Hotel mockHotel = Hotel.builder()
                .id(100L)
                .name("Hotel Test")
                .build();

        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .role(Role.ADMIN)
                .hotel(mockHotel)
                .build();

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtService.generateToken(mockUser)).thenReturn("mock-jwt");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt", response.getToken());
        assertEquals(100L, response.getHotelId());
        assertEquals("testuser", response.getUsername());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(mockUser);
    }

    @Test
    void login_debeLanzarExcepcionSiUsuarioNoTieneHotel() {
        // Arrange
        AuthRequest request = new AuthRequest("testuser", "123456");

        User mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded")
                .role(Role.ADMIN)
                .hotel(null) // Sin hotel asociado
                .build();

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                authService.login(request)
        );

        assertEquals("No se encontró el hotel del usuario", ex.getMessage());
    }

    @Test
    void register_debeGuardarUsuarioYHotelConRelacion() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("123456")
                .email("new@example.com")
                .hotelName("Hotel Nuevo")
                .address("Calle 123")
                .city("CiudadX")
                .country("PaisX")
                .phone("987654321")
                .description("Un hotel de prueba")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-pass");

        Hotel savedHotel = Hotel.builder()
                .id(200L)
                .name("Hotel Nuevo")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("newuser")
                .email("new@example.com")
                .password("encoded-pass")
                .role(Role.ADMIN)
                .hotel(savedHotel)
                .build();

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals(200L, response.getHotelId());
        assertEquals("newuser", response.getUsername());

        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("123456");

        // Verificar que el hotel se guardó primero
        verify(hotelRepository).save(argThat(hotel ->
                hotel.getName().equals("Hotel Nuevo")
        ));

        // Verificar que el usuario se guardó con el hotel asignado
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser") &&
                        user.getHotel() != null &&
                        user.getRole() == Role.ADMIN
        ));
    }

    @Test
    void register_debeLanzarExcepcionSiUsuarioExiste() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("123456")
                .email("email@example.com")
                .hotelName("Hotel Existente")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
                authService.register(request)
        );

        assertEquals("El usuario ya existe", ex.getMessage());
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).save(any());
        verify(hotelRepository, never()).save(any());
    }

    @Test
    void register_debeAsignarPasswordEncriptadoAntesDeGuardar() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .username("secureuser")
                .password("plainpass")
                .email("secure@example.com")
                .hotelName("Secure Hotel")
                .build();

        when(userRepository.existsByUsername("secureuser")).thenReturn(false);
        when(passwordEncoder.encode("plainpass")).thenReturn("encoded-pass");
        when(hotelRepository.save(any())).thenReturn(Hotel.builder().id(10L).build());
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any())).thenReturn("token");

        // Act
        authService.register(request);

        // Assert
        verify(passwordEncoder).encode("plainpass");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded-pass")
        ));
    }
}
