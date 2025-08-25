package com.hotelsa.backend.user.service;

import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.dto.UserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.exception.UserNotFoundException;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @AdminOnly
    public UserDto registerEmployee(RegisterUserDto dto) {
        User currentUser = getCurrentUser();

        // Crear el nuevo empleado
        User employee = userMapper.fromRegisterDto(dto);
        employee.setRole(Role.USER);
        employee.setHotel(currentUser.getHotel());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Guardar y convertir a DTO seguro
        User savedUser = userRepository.save(employee);
        return userMapper.fromEntity(savedUser);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    @AdminOnly
    public List<UserDto> getUsersByHotel() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<User> users = userRepository.findByHotelId(currentUser.getHotel().getId());
        return users.stream()
                .map(userMapper::fromEntity)
                .toList();
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.fromEntity(user);
    }

    @AdminOnly
    public UserDto updateUser(Long id, RegisterUserDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

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
        return userMapper.fromEntity(updatedUser);
    }
}
