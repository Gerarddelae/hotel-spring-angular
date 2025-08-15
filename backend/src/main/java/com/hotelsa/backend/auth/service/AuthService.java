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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        // Buscar hotel asociado al usuario
        Hotel hotel = user.getHotel();
        if (hotel == null) {
            throw new IllegalStateException("No se encontró el hotel del usuario");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, hotel.getId(), user.getUsername());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("El usuario ya existe");
        }

        // Crear usuario ADMIN
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        // Crear hotel con datos del request
        Hotel hotel = Hotel.builder()
                .name(request.getHotelName())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .phone(request.getPhone())
                .description(request.getDescription())
                .build();

        // Guardar hotel primero
        Hotel savedHotel = hotelRepository.save(hotel);

        // Asociar usuario al hotel
        user.setHotel(savedHotel);
        User savedUser = userRepository.save(user);

        // Generar token JWT
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, savedHotel.getId(), savedUser.getUsername());
    }


}
