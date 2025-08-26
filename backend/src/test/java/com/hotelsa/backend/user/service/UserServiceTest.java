package com.hotelsa.backend.user.service;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.exception.UserNotFoundException;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks private UserService userService;

    private User currentUser;
    private User employee;
    private RegisterUserDto registerUserDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        // ✅ Crear hotel ficticio y asociarlo a los usuarios
        Hotel hotel = new Hotel();
        hotel.setId(100L);
        hotel.setName("Hotel Paradise");
        hotel.setCity("Madrid");
        hotel.setCountry("Spain");

        // Usuario actual (admin)
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("admin");
        currentUser.setEmail("admin@hotel.com");
        currentUser.setRole(Role.ADMIN);

        // Empleado
        employee = new User();
        employee.setId(2L);
        employee.setUsername("employee");
        employee.setEmail("employee@hotel.com");
        employee.setRole(Role.USER);

        // ✅ Mantener consistencia bidireccional
        hotel.addUser(currentUser);
        hotel.addUser(employee);

        // DTO para registro
        registerUserDto = RegisterUserDto.builder()
                .username("newemployee")
                .password("password123")
                .email("newemployee@hotel.com")
                .build();

        // DTO de usuario
        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setUsername("employee");
        userDto.setEmail("employee@hotel.com");
    }

    private void mockAuthenticatedUser(User user) {
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void registerEmployee_ShouldCreateEmployee_WhenValidData() {
        mockAuthenticatedUser(currentUser); // Necesario para registerEmployee()

        when(userMapper.fromRegisterDto(registerUserDto)).thenReturn(employee);
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(employee);
        when(userMapper.fromEntity(employee)).thenReturn(userDto);

        UserDto result = userService.registerEmployee(registerUserDto);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUsersByHotel_ShouldReturnUsersList_WhenUsersExist() {
        mockAuthenticatedUser(currentUser); // Necesario para getUsersByHotel()

        List<User> users = Arrays.asList(currentUser, employee);
        when(userRepository.findByHotelId(anyLong())).thenReturn(users);
        when(userMapper.fromEntity(any(User.class))).thenReturn(userDto);

        List<UserDto> result = userService.getUsersByHotel();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByHotelId(anyLong());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userMapper.fromEntity(employee)).thenReturn(userDto);

        UserDto result = userService.getUserById(2L);

        assertNotNull(result);
        assertEquals(userDto.getUsername(), result.getUsername());
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidData() {
        RegisterUserDto updateDto = RegisterUserDto.builder()
                .username("updatedEmployee")
                .password("newPassword")
                .email("updated@hotel.com")
                .build();

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("employee");
        existingUser.setEmail("employee@hotel.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(updateDto.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(updateDto.getPassword())).thenReturn("encodedNewPassword");
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.fromEntity(existingUser)).thenReturn(userDto);

        UserDto result = userService.updateUser(2L, updateDto);

        assertEquals("updatedEmployee", existingUser.getUsername());
        assertEquals("updated@hotel.com", existingUser.getEmail());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(999L, registerUserDto));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUsernameAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("oldUsername");

        RegisterUserDto updateDto = RegisterUserDto.builder()
                .username("existingUsername")
                .password("password123")
                .email("updated@hotel.com")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(updateDto.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(2L, updateDto));
    }

    @Test
    void updateUser_ShouldNotUpdatePassword_WhenPasswordIsBlank() {
        RegisterUserDto updateDto = RegisterUserDto.builder()
                .username("updatedEmployee")
                .password("")
                .email("updated@hotel.com")
                .build();

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("employee");
        existingUser.setEmail("employee@hotel.com");
        existingUser.setPassword("oldEncodedPassword");

        when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername(updateDto.getUsername())).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.fromEntity(existingUser)).thenReturn(userDto);

        UserDto result = userService.updateUser(2L, updateDto);

        assertEquals("oldEncodedPassword", existingUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_ShouldSoftDeleteUser_WhenUserExists() {
        User userToDelete = new User();
        userToDelete.setId(2L);
        userToDelete.setUsername("employee");
        userToDelete.setDeleted(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(userToDelete));
        when(userRepository.save(userToDelete)).thenReturn(userToDelete);

        userService.deleteUser(2L);

        assertTrue(userToDelete.isDeleted());
        verify(userRepository).save(userToDelete);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
    }
}
