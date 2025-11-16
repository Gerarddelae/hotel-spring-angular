package com.hotelsa.backend.user.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.tenant.TenantContext;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.exception.UserNotFoundException;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ✅ Obtener usuario autenticado
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    // ✅ Obtener tenant actual (del contexto)
    private Long getCurrentTenantId() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            tenantId = getCurrentUser().getHotelId();
        }
        return tenantId;
    }

    // ✅ Registrar empleado (por admin)
    @AdminOnly
    @Transactional
    public UserDto registerEmployee(RegisterUserDto dto) {
        User currentUser = getCurrentUser();

        User employee = userMapper.fromRegisterDto(dto);
        employee.setRole(Role.USER);
        employee.setHotelId(currentUser.getHotelId());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(employee);
        log.debug("Registered new employee: {} for hotel: {}", savedUser.getUsername(), savedUser.getHotelId());

        return userMapper.fromEntity(savedUser);
    }

    // ✅ Obtener todos los usuarios del hotel (filtros ya aplicados automáticamente)
    @AdminOnly
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByHotel() {
        List<User> users = userRepository.findAll();
        log.debug("Found {} users for hotel {}", users.size(), getCurrentTenantId());
        return users.stream().map(userMapper::fromEntity).toList();
    }

    // ✅ Obtener usuario por ID (ya filtrado por tenant y deleted)
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found or not accessible"));
        return userMapper.fromEntity(user);
    }

    // ✅ Actualizar usuario
    @AdminOnly
    @Transactional
    public UserDto updateUser(Long id, RegisterUserDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found or not accessible"));

        if (!existingUser.getUsername().equals(dto.getUsername()) &&
                userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        existingUser.setUsername(dto.getUsername());
        existingUser.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        log.debug("Updated user: {} for hotel: {}", updatedUser.getUsername(), updatedUser.getHotelId());

        return userMapper.fromEntity(updatedUser);
    }

    // ✅ Soft delete (ya respetando tenant)
    @AdminOnly
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found or not accessible"));

        user.setDeleted(true);
        userRepository.save(user);

        log.debug("Soft deleted user: {} for hotel: {}", user.getUsername(), user.getHotelId());
    }
}
