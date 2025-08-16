package com.hotelsa.backend.auth.service;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.hotel.mapper.HotelMapper;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.mapper.UserMapper;
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
    private final UserMapper userMapper;
    private final HotelMapper hotelMapper;

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
        // Extraer DTOs
        var userDto = request.getUser();
        var hotelDto = request.getHotel();

        // Validar si el usuario ya existe
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("El usuario ya existe");
        }

        // Mapear DTOs a entidades usando los mappers
        User user = userMapper.fromRegisterDto(userDto);
        Hotel hotel = hotelMapper.fromRegisterDto(hotelDto);

        // Guardar hotel
        Hotel savedHotel = hotelRepository.save(hotel);

        // Asociar usuario con el hotel y guardar
        user.setHotel(savedHotel);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // Generar token JWT
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, savedHotel.getId(), savedUser.getUsername());
    }


}
