package com.hotelsa.backend.user.service;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = Hotel.builder()
                .id(1L)
                .name("Hotel Test")
                .address("123 Street")
                .city("Test City")
                .country("Testland")
                .phone("+123456789")
                .description("Test description")
                .build();
    }

    private void mockAuthenticatedUser(User user) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldRegisterEmployeeSuccessfully() {
        // Arrange: admin autenticado
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@mail.com")
                .password("securePassword")
                .role(Role.ADMIN)
                .hotel(hotel)
                .build();
        mockAuthenticatedUser(adminUser);

        // DTO de entrada
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("employee1")
                .email("employee@mail.com")
                .password("password123")
                .build();

        // Entidad que será creada
        User employeeEntity = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password("encodedPassword")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        // DTO de salida
        UserDto employeeDto = UserDto.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .role(Role.USER)
                .hotelId(hotel.getId())
                .build();

        // Mocks
        when(userMapper.fromRegisterDto(dto)).thenReturn(employeeEntity);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(employeeEntity);
        when(userMapper.fromEntity(employeeEntity)).thenReturn(employeeDto);

        // Act
        UserDto savedUser = userService.registerEmployee(dto);

        // Assert
        assertNotNull(savedUser);
        assertEquals(dto.getUsername(), savedUser.getUsername());
        assertEquals(dto.getEmail(), savedUser.getEmail());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(hotel.getId(), savedUser.getHotelId());
    }

    @Test
    void shouldReturnUsersOfSameHotel() {
        // Arrange
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@mail.com")
                .password("securePassword")
                .role(Role.ADMIN)
                .hotel(hotel)
                .build();
        mockAuthenticatedUser(adminUser);

        User user1 = User.builder()
                .id(2L)
                .username("employee1")
                .email("employee1@mail.com")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        User user2 = User.builder()
                .id(3L)
                .username("employee2")
                .email("employee2@mail.com")
                .role(Role.USER)
                .hotel(hotel)
                .build();

        List<User> users = List.of(user1, user2);

        when(userRepository.findByHotelId(hotel.getId())).thenReturn(users);
        when(userMapper.fromEntity(user1)).thenReturn(UserDto.builder()
                .username("employee1")
                .email("employee1@mail.com")
                .role(Role.USER)
                .hotelId(hotel.getId())
                .build());
        when(userMapper.fromEntity(user2)).thenReturn(UserDto.builder()
                .username("employee2")
                .email("employee2@mail.com")
                .role(Role.USER)
                .hotelId(hotel.getId())
                .build());

        // Act
        List<UserDto> result = userService.getUsersByHotel();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("employee1", result.get(0).getUsername());
        assertEquals("employee2", result.get(1).getUsername());
    }

    @Test
    void shouldReturnEmptyListIfNoUsers() {
        // Arrange
        User adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .hotel(hotel)
                .build();
        mockAuthenticatedUser(adminUser);

        when(userRepository.findByHotelId(hotel.getId())).thenReturn(List.of());

        // Act
        List<UserDto> result = userService.getUsersByHotel();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
