package com.hotelsa.backend.bookingaddon.repository;

import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.entity.BookingAddonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingAddonRepository extends JpaRepository<BookingAddon, BookingAddonId> {

    // Consultas que incluyen hotelId para filtrado de tenant
    List<BookingAddon> findByIdBookingIdAndHotelId(Long bookingId, Long hotelId);

    List<BookingAddon> findByIdAddonIdAndHotelId(Long addonId, Long hotelId);

    boolean existsByIdBookingIdAndIdAddonIdAndHotelId(Long bookingId, Long addonId, Long hotelId);

    Optional<BookingAddon> findByIdAndHotelId(BookingAddonId id, Long hotelId);
}
