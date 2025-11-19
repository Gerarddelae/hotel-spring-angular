package com.hotelsa.backend.bill.mapper;

import com.hotelsa.backend.bill.dto.BillRequestDTO;
import com.hotelsa.backend.bill.dto.BillResponseDTO;
import com.hotelsa.backend.bill.model.Bill;
import com.hotelsa.backend.billaddon.mapper.BillAddonMapper;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    private final BillAddonMapper billAddonMapper;

    public BillMapper(BillAddonMapper billAddonMapper) {
        this.billAddonMapper = billAddonMapper;
    }

    public Bill fromRequestDto(BillRequestDTO dto) {
        Bill bill = Bill.builder()
                .notes(dto.getNotes())
                .status(dto.getStatus() == null ? com.hotelsa.backend.bill.enums.BillStatus.UNPAID : dto.getStatus())
                .paymentMethod(dto.getPaymentMethod())
                .build();
        return bill;
    }

    public BillResponseDTO fromEntity(Bill entity) {
        List<BillAddon> addons = entity.getAddons();

        return BillResponseDTO.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking() == null ? null : entity.getBooking().getId())
                .notes(entity.getNotes())
                .status(entity.getStatus() == null ? null : entity.getStatus().name())
                .paymentMethod(entity.getPaymentMethod() == null ? null : entity.getPaymentMethod().name())
                .createdAt(entity.getCreatedAt())
                .totalAmount(entity.getTotalAmount())
                .addons(billAddonMapper.fromEntityList(addons))
                .build();
    }

    public List<BillResponseDTO> fromEntityList(List<Bill> list) {
        return list == null ? java.util.Collections.emptyList() : list.stream().map(this::fromEntity).collect(Collectors.toList());
    }
}
