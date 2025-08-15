package com.hotelsa.backend.hotel.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
