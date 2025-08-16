package com.hotelsa.backend.user.mapper;

import com.hotelsa.backend.user.dto.RegisterUserDto;
import com.hotelsa.backend.user.enums.Role;
import com.hotelsa.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User fromRegisterDto(RegisterUserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // ¡OJO! La encriptación se hace en el servicio
        user.setRole(Role.ADMIN); // por defecto ADMIN para registro
        return user;
    }
}
