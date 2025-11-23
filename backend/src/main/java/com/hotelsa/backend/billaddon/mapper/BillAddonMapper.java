package com.hotelsa.backend.billaddon.mapper;

import com.hotelsa.backend.billaddon.dto.BillAddonResponseDTO;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillAddonMapper {

    public BillAddonResponseDTO fromEntity(BillAddon entity) {
        return BillAddonResponseDTO.builder()
                .addonId(entity.getAddonId())
                .addonName(entity.getAddonName())
                .description(entity.getAddonDescription())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .totalPrice(entity.getTotalPrice())
                .build();
    }

    public List<BillAddonResponseDTO> fromEntityList(List<BillAddon> list) {
        return list == null ? java.util.Collections.emptyList() : list.stream().map(this::fromEntity).collect(Collectors.toList());
    }
}
