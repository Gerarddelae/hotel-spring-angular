package com.hotelsa.backend.bookingaddon.repository;

import com.hotelsa.backend.bookingaddon.entity.BookingAddon;
import com.hotelsa.backend.bookingaddon.entity.BookingAddonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingAddonRepository extends JpaRepository<BookingAddon, BookingAddonId> {

    List<BookingAddon> findByIdBookingId(Long bookingId);

    List<BookingAddon> findByIdAddonId(Long addonId);

    boolean existsByIdBookingIdAndIdAddonId(Long bookingId, Long addonId);
}
