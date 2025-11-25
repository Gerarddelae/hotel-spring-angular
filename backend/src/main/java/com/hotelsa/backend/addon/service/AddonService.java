package com.hotelsa.backend.addon.service;

import com.hotelsa.backend.addon.dto.AddonRequest;
import com.hotelsa.backend.addon.dto.AddonResponse;
import com.hotelsa.backend.addon.exception.AddonNotFoundException;
import com.hotelsa.backend.addon.mapper.AddonMapper;
import com.hotelsa.backend.addon.model.Addon;
import com.hotelsa.backend.addon.repository.AddonRepository;
import com.hotelsa.backend.aop.annotation.AdminOnly;
import com.hotelsa.backend.auth.service.AuthService;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddonService {

    private final AddonRepository addonRepository;
    private final AddonMapper addonMapper;
    private final HotelRepository hotelRepository;
    private final AuthService authService;

    private Long getCurrentHotelId() {
        return authService.getCurrentHotelId();
    }

    @Transactional
    @AdminOnly
    public AddonResponse create(AddonRequest request) {
        if (addonRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Ya existe un addon con ese nombre");
        }

        Long hotelId = getCurrentHotelId();

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException("Hotel no encontrado"));

        Addon addon = addonMapper.fromRequestDto(request);
        addon.setHotelId(hotelId);
        Addon saved = addonRepository.save(addon);
        log.debug("✅ Created addon {}", saved.getName());
        return addonMapper.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public AddonResponse findById(Long id) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new AddonNotFoundException("Addon no encontrado"));
        return addonMapper.fromEntity(addon);
    }

    @Transactional(readOnly = true)
    public List<AddonResponse> findAll() {
        List<Addon> list = addonRepository.findAll();
        return addonMapper.fromEntityList(list);
    }

    @Transactional
    @AdminOnly
    public AddonResponse update(Long id, AddonRequest request) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new AddonNotFoundException("Addon no encontrado"));

        if (!addon.getName().equalsIgnoreCase(request.getName()) &&
                addonRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Ya existe un addon con ese nombre");
        }

        addon.setName(request.getName());
        addon.setDescription(request.getDescription());
        addon.setPrice(request.getPrice());

        Addon updated = addonRepository.save(addon);
        log.debug("✅ Updated addon {}", updated.getName());
        return addonMapper.fromEntity(updated);
    }

    @Transactional
    @AdminOnly
    public void delete(Long id) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new AddonNotFoundException("Addon no encontrado"));
        addon.setDeleted(true);
        addonRepository.save(addon);
        log.debug("🗑️ Soft deleted addon {}", addon.getName());
    }

    @Transactional(readOnly = true)
    public List<AddonResponse> searchByName(String name) {
        List<Addon> list = addonRepository.findByNameContainingIgnoreCase(name);
        return addonMapper.fromEntityList(list);
    }

    // Nuevo: retornar solo los addons activos
    @Transactional(readOnly = true)
    public List<AddonResponse> getAllActive() {
        List<Addon> list = addonRepository.findAllByActiveTrue();
        return addonMapper.fromEntityList(list);
    }

    // Nuevo: alternar el campo active del addon
    @Transactional
    public AddonResponse toggleActive(Long id) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new AddonNotFoundException("Addon no encontrado"));
        addon.setActive(!addon.isActive());
        Addon saved = addonRepository.save(addon);
        log.debug("🔁 Toggled active for addon {} -> {}", saved.getName(), saved.isActive());
        return addonMapper.fromEntity(saved);
    }
}
