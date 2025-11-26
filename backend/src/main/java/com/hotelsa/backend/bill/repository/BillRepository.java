package com.hotelsa.backend.bill.repository;

import com.hotelsa.backend.bill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // Dashboard queries - Revenue
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.status = 'PAID'")
    BigDecimal sumTotalRevenue();

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0)
            FROM Bill b
            WHERE b.status = 'PAID'
            AND CAST(b.createdAt AS date) = :date
            """)
    BigDecimal sumRevenueByDate(@Param("date") LocalDate date);

    @Query("""
            SELECT COALESCE(SUM(b.totalAmount), 0)
            FROM Bill b
            WHERE b.status = 'PAID'
            AND EXTRACT(MONTH FROM b.createdAt) = :month
            AND EXTRACT(YEAR FROM b.createdAt) = :year
            """)
    BigDecimal sumRevenueByMonth(@Param("month") int month, @Param("year") int year);
}
