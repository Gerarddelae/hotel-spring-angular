package com.hotelsa.backend.bill.repository;

import com.hotelsa.backend.bill.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByHotelId(Long hotelId);
}
