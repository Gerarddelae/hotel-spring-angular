package com.hotelsa.backend.auth.service;

import com.hotelsa.backend.auth.dto.AuthRequest;
import com.hotelsa.backend.auth.dto.AuthResponse;
import com.hotelsa.backend.auth.dto.RegisterRequest;
import com.hotelsa.backend.hotel.mapper.HotelMapper;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.user.exception.UserAlreadyExistsException;
import com.hotelsa.backend.user.mapper.UserMapper;
import com.hotelsa.backend.user.model.User;
import com.hotelsa.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Hotel hotel = user.getHotel();
        if (hotel == null) {
            throw new IllegalStateException("No se encontró el hotel del usuario");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getUsername(),
                hotel.getName(),
                List.of(user.getRole().name()) // Solo el nombre del role, sin prefijo
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        var userDto = request.getUser();
        var hotelDto = request.getHotel();

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("El usuario ya existe");
        }

        User user = userMapper.fromRegisterDto(userDto);
        Hotel hotel = hotelMapper.fromRegisterDto(hotelDto);

        Hotel savedHotel = hotelRepository.save(hotel);

        user.setHotel(savedHotel);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getUsername(),
                savedHotel.getName(),
                List.of(savedUser.getRole().name())
        );
    }
}
