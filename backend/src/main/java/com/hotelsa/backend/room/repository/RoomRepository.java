package com.hotelsa.backend.room.repository;

import com.hotelsa.backend.room.enums.RoomType;
import com.hotelsa.backend.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // ✅ El filtro de tenant y deleted se aplica automáticamente
    boolean existsByNumber(String number);

    List<Room> findByType(RoomType type);
}
