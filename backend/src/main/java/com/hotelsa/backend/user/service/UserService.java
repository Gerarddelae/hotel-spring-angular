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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    /**
     * Activa automáticamente el filtro para "usuarios activos".
     * Evita errores en entornos de prueba donde no hay sesión real de Hibernate.
     */
    private void activarFiltroSoftDelete() {
        if (entityManager == null) {
            return; // Evita NPE en tests unitarios
        }
        try {
            Session session = entityManager.unwrap(Session.class);
            if (session != null) {
                Filter filter = session.enableFilter("deletedFilter");
                filter.setParameter("isDeleted", false);
            }
        } catch (Exception e) {
            // Estamos en un contexto donde no hay sesión real (mock o test)
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("User is not authenticated");
    }

    @AdminOnly
    @Transactional
    public UserDto registerEmployee(RegisterUserDto dto) {
        User currentUser = getCurrentUser();

        User employee = userMapper.fromRegisterDto(dto);
        employee.setRole(Role.USER);
        employee.setHotel(currentUser.getHotel());
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(employee);
        return userMapper.fromEntity(savedUser);
    }

    @AdminOnly
    @Transactional(readOnly = true)
    public List<UserDto> getUsersByHotel() {
        activarFiltroSoftDelete(); // Aplica el filtro automáticamente
        User currentUser = getCurrentUser();
        List<User> users = userRepository.findByHotelId(currentUser.getHotel().getId());
        return users.stream().map(userMapper::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        activarFiltroSoftDelete(); // Filtra usuarios eliminados
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.fromEntity(user);
    }

    @AdminOnly
    @Transactional
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

    @AdminOnly
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Soft delete
        user.setDeleted(true);
        userRepository.save(user);
    }
}
