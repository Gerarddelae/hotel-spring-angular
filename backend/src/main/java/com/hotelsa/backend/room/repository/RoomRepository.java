package com.hotelsa.backend.room.repository;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // ✅ El filtro de tenant y deleted se aplica automáticamente
    boolean existsByNumber(String number);

    List<Room> findByType(RoomType type);

    // Busca habitaciones del hotel que NO tienen bookings no-canceladas que se solapen con el rango
    @Query("SELECT r FROM Room r WHERE r.hotelId = :hotelId " +
            "AND NOT EXISTS (" +
            "  SELECT b FROM Booking b WHERE b.room = r " +
            "    AND b.status <> :cancelled " +
            "    AND b.checkInDate < :checkOut " +
            "    AND b.checkOutDate > :checkIn" +
            ")")
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId,
                                  @Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("cancelled") BookingStatus cancelled);
}
