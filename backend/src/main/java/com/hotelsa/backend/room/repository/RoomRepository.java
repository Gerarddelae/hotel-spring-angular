package com.hotelsa.backend.room.repository;

import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.room.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByNumberAndHotel_Id(String number, Long hotelId);

    List<Room> findByHotel_Id(Long hotelId);

    List<Room> findByHotel_IdAndType(Long hotelId, RoomType type);
}
