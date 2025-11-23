package com.hotelsa.backend.billaddon.repository;

import com.hotelsa.backend.billaddon.entity.BillAddon;
import com.hotelsa.backend.billaddon.entity.BillAddonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillAddonRepository extends JpaRepository<BillAddon, BillAddonId> {

    List<BillAddon> findByIdBillIdAndHotelId(Long billId, Long hotelId);

    boolean existsByIdBillIdAndIdAddonIdAndHotelId(Long billId, Long addonId, Long hotelId);

    Optional<BillAddon> findByIdAndHotelId(BillAddonId id, Long hotelId);
}
