package com.hotelsa.backend.bill.repository;

import com.hotelsa.backend.bill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByHotelId(Long hotelId);

    @Query("SELECT b FROM Bill b " +
           "LEFT JOIN FETCH b.booking booking " +
           "LEFT JOIN FETCH booking.guest " +
           "LEFT JOIN FETCH booking.room " +
           "LEFT JOIN FETCH b.addons " +
           "WHERE b.id = :id")
    Optional<Bill> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Bill b " +
           "LEFT JOIN FETCH b.booking booking " +
           "LEFT JOIN FETCH booking.guest " +
           "LEFT JOIN FETCH booking.room")
    List<Bill> findAllWithRelations();
}
