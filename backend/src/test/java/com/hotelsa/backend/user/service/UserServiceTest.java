package com.hotelsa.backend.user.service;

import com.hotelsa.backend.tenant.TenantContext;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.exception.UserNotFoundException;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        // ✅ Configurar tenant ficticio
        TenantContext.setCurrentTenant(1L);

        // ✅ Usuario actual simulado (admin)
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("admin");
        currentUser.setEmail("admin@hotel.com");
        currentUser.setRole(Role.ADMIN);
        currentUser.setHotelId(1L);

        // ✅ Usuario empleado
        employee = new User();
        employee.setId(2L);
        employee.setUsername("employee");
        employee.setEmail("employee@hotel.com");
        employee.setRole(Role.USER);
        employee.setHotelId(1L);

        // ✅ DTO de registro
        registerUserDto = RegisterUserDto.builder()
                .username("newemployee")
                .password("password123")
                .email("newemployee@hotel.com")
                .build();

        // ✅ DTO de respuesta
        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setUsername("employee");
        userDto.setEmail("employee@hotel.com");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(User user) {
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // 🔹 TEST: registrar empleado
    @Test
    void registerEmployee_ShouldCreateEmployee_WhenValidData() {
        mockAuthenticatedUser(currentUser);

        when(userMapper.fromRegisterDto(registerUserDto)).thenReturn(employee);
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(employee);
        when(userMapper.fromEntity(employee)).thenReturn(userDto);

        UserDto result = userService.registerEmployee(registerUserDto);

        assertNotNull(result);
        assertEquals("employee", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    // 🔹 TEST: obtener todos los usuarios (findAll con multitenant)
    @Test
    void getUsersByHotel_ShouldReturnUsersList_WhenUsersExist() {
        mockAuthenticatedUser(currentUser);

        List<User> users = Arrays.asList(currentUser, employee);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.fromEntity(any(User.class))).thenReturn(userDto);

        List<UserDto> result = userService.getUsersByHotel();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // 🔹 TEST: obtener usuario por ID
    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(userMapper.fromEntity(employee)).thenReturn(userDto);

        UserDto result = userService.getUserById(2L);

        assertNotNull(result);
        assertEquals("employee", result.getUsername());
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    // 🔹 TEST: actualizar usuario
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
        verify(userRepository).save(existingUser);
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

    // 🔹 TEST: eliminar usuario
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
