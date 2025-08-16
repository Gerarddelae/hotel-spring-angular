package com.hotelsa.backend.auth.service;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.hotel.dto.RegisterHotelDto;
import com.hotelsa.backend.hotel.mapper.HotelMapper;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.mapper.UserMapper;
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

    @Mock
    private UserMapper userMapper;

    @Mock
    private HotelMapper hotelMapper;

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
    void register_debeGuardarUsuarioConPasswordEncriptadaYHotel() {
        // Arrange: DTOs de prueba
        RegisterUserDto userDto = RegisterUserDto.builder()
                .username("secureuser")
                .password("plainpass")
                .email("secure@example.com")
                .build();

        RegisterHotelDto hotelDto = RegisterHotelDto.builder()
                .name("Secure Hotel")
                .address("Calle Segura 123")
                .city("CiudadSegura")
                .country("PaisSegura")
                .phone("123456789")
                .description("Hotel seguro")
                .build();

        RegisterRequest request = new RegisterRequest(userDto, hotelDto);

        // Objetos que devolverán los mappers
        User mappedUser = new User();
        mappedUser.setUsername(userDto.getUsername());
        mappedUser.setEmail(userDto.getEmail());
        mappedUser.setPassword(userDto.getPassword());
        mappedUser.setRole(Role.ADMIN);

        Hotel mappedHotel = new Hotel();
        mappedHotel.setName(hotelDto.getName());
        mappedHotel.setAddress(hotelDto.getAddress());
        mappedHotel.setCity(hotelDto.getCity());
        mappedHotel.setCountry(hotelDto.getCountry());
        mappedHotel.setPhone(hotelDto.getPhone());
        mappedHotel.setDescription(hotelDto.getDescription());

        // Objetos "guardados"
        Hotel savedHotel = Hotel.builder().id(10L).name(mappedHotel.getName()).build();
        User savedUser = new User();
        savedUser.setUsername(mappedUser.getUsername());
        savedUser.setEmail(mappedUser.getEmail());
        savedUser.setPassword("encoded-pass");
        savedUser.setRole(Role.ADMIN);
        savedUser.setHotel(savedHotel);

        // Stubbing de mappers
        when(userMapper.fromRegisterDto(any(RegisterUserDto.class))).thenReturn(mappedUser);
        when(hotelMapper.fromRegisterDto(any(RegisterHotelDto.class))).thenReturn(mappedHotel);

        // Stubbing de repositorios y encoder
        when(userRepository.existsByUsername("secureuser")).thenReturn(false);
        when(passwordEncoder.encode("plainpass")).thenReturn("encoded-pass");
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals(10L, response.getHotelId());
        assertEquals("secureuser", response.getUsername());

        // Verificar que la contraseña fue encriptada antes de guardar
        verify(passwordEncoder).encode("plainpass");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded-pass") &&
                        user.getUsername().equals("secureuser") &&
                        user.getHotel() != null
        ));

        // Verificar que el hotel se guardó
        verify(hotelRepository).save(argThat(hotel ->
                hotel.getName().equals("Secure Hotel")
        ));
    }




    @Test
    void register_debeLanzarExcepcionSiUsuarioExiste() {
        // Arrange
        RegisterUserDto userDto = RegisterUserDto.builder()
                .username("existinguser")
                .password("123456")
                .email("email@example.com")
                .build();

        RegisterHotelDto hotelDto = RegisterHotelDto.builder()
                .name("Hotel Existente")
                .build();

        RegisterRequest request = new RegisterRequest(userDto, hotelDto);

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
        // Arrange: DTOs de prueba
        RegisterUserDto userDto = RegisterUserDto.builder()
                .username("secureuser")
                .password("plainpass")
                .email("secure@example.com")
                .build();

        RegisterHotelDto hotelDto = RegisterHotelDto.builder()
                .name("Secure Hotel")
                .address("Calle Segura 123")
                .city("CiudadSegura")
                .country("PaisSegura")
                .phone("123456789")
                .description("Hotel seguro")
                .build();

        RegisterRequest request = new RegisterRequest(userDto, hotelDto);

        // Objetos que devolverán los mappers
        User mappedUser = new User();
        mappedUser.setUsername(userDto.getUsername());
        mappedUser.setEmail(userDto.getEmail());
        mappedUser.setPassword(userDto.getPassword());
        mappedUser.setRole(Role.ADMIN);

        Hotel mappedHotel = new Hotel();
        mappedHotel.setName(hotelDto.getName());
        mappedHotel.setAddress(hotelDto.getAddress());
        mappedHotel.setCity(hotelDto.getCity());
        mappedHotel.setCountry(hotelDto.getCountry());
        mappedHotel.setPhone(hotelDto.getPhone());
        mappedHotel.setDescription(hotelDto.getDescription());

        // Objetos "guardados"
        Hotel savedHotel = Hotel.builder().id(10L).name(mappedHotel.getName()).build();
        User savedUser = new User();
        savedUser.setUsername(mappedUser.getUsername());
        savedUser.setEmail(mappedUser.getEmail());
        savedUser.setPassword("encoded-pass");
        savedUser.setRole(Role.ADMIN);
        savedUser.setHotel(savedHotel);

        // Stubbing de mappers
        when(userMapper.fromRegisterDto(any(RegisterUserDto.class))).thenReturn(mappedUser);
        when(hotelMapper.fromRegisterDto(any(RegisterHotelDto.class))).thenReturn(mappedHotel);

        // Stubbing de repositorios y encoder
        when(userRepository.existsByUsername("secureuser")).thenReturn(false);
        when(passwordEncoder.encode("plainpass")).thenReturn("encoded-pass");
        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-token");

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals(10L, response.getHotelId());
        assertEquals("secureuser", response.getUsername());

        // Verificar que la contraseña fue encriptada antes de guardar
        verify(passwordEncoder).encode("plainpass");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded-pass") &&
                        user.getUsername().equals("secureuser") &&
                        user.getEmail().equals("secure@example.com") &&
                        user.getHotel() != null
        ));

        // Verificar que el hotel se guardó
        verify(hotelRepository).save(argThat(hotel ->
                hotel.getName().equals("Secure Hotel")
        ));
    }



}
