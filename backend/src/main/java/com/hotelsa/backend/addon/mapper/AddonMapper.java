package com.hotelsa.backend.addon.mapper;

import com.hotelsa.backend.addon.dto.AddonRequest;
import com.hotelsa.backend.addon.dto.AddonResponse;
import com.hotelsa.backend.addon.model.Addon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddonMapper {

    // Mapea un DTO de request a una entidad Addon (sin gestionar relaciones)
    public Addon fromRequestDto(AddonRequest dto) {
        Addon addon = new Addon();
        addon.setName(dto.getName());
        addon.setDescription(dto.getDescription());
        addon.setPrice(dto.getPrice());
        return addon;
    }

    // Mapea una entidad Addon a un DTO de respuesta
    public AddonResponse fromEntity(Addon addon) {
        return AddonResponse.builder()
                .id(addon.getId())
                .name(addon.getName())
                .description(addon.getDescription())
                .price(addon.getPrice())
                .createdAt(addon.getCreatedAt())
                .build();
    }

    // Mapea una entidad Addon a un DTO de respuesta con cantidad y subtotal (para BookingAddon)
    public AddonResponse fromEntityWithQuantity(Addon addon, Integer quantity, Integer subtotal) {
        return AddonResponse.builder()
                .id(addon.getId())
                .name(addon.getName())
                .description(addon.getDescription())
                .price(addon.getPrice())
                .createdAt(addon.getCreatedAt())
                .quantity(quantity)
                .subtotal(subtotal)
                .build();
    }

    public List<AddonResponse> fromEntityList(List<Addon> addons) {
        return addons.stream().map(this::fromEntity).collect(Collectors.toList());
    }
}
