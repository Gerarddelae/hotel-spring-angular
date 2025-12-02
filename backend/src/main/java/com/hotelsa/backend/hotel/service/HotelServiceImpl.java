package com.hotelsa.backend.hotel.service;

import com.hotelsa.backend.hotel.dto.HotelResponse;
import com.hotelsa.backend.hotel.dto.HotelUpdateRequest;
import com.hotelsa.backend.hotel.exception.HotelNotFoundException;
import com.hotelsa.backend.hotel.mapper.HotelMapper;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.hotel.repository.HotelRepository;
import com.hotelsa.backend.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    @Transactional
    public HotelResponse updateHotel(Long id, HotelUpdateRequest request) {
        log.debug("Actualizando hotel completo con ID: {}", id);

        Hotel hotel = findHotelAndValidateTenant(id);

        // Actualizar todos los campos (PUT - reemplazo completo)
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setCountry(request.getCountry());
        hotel.setPhone(request.getPhone());
        hotel.setDescription(request.getDescription());

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.debug("✅ Hotel {} actualizado completamente", updatedHotel.getId());

        return hotelMapper.toResponse(updatedHotel);
    }

    @Override
    @Transactional
    public HotelResponse patchHotel(Long id, HotelUpdateRequest request) {
        log.debug("Actualizando hotel parcialmente con ID: {}", id);

        Hotel hotel = findHotelAndValidateTenant(id);

        // Actualizar solo los campos no nulos (PATCH - actualización parcial)
        hotelMapper.updateEntityFromRequest(request, hotel);

        Hotel updatedHotel = hotelRepository.save(hotel);
        log.debug("✅ Hotel {} actualizado parcialmente", updatedHotel.getId());

        return hotelMapper.toResponse(updatedHotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long id) {
        log.debug("Buscando hotel con ID: {}", id);

        Hotel hotel = findHotelAndValidateTenant(id);

        return hotelMapper.toResponse(hotel);
    }

    /**
     * Busca un hotel por ID y valida que pertenezca al tenant actual.
     *
     * @param id ID del hotel
     * @return Hotel encontrado
     * @throws HotelNotFoundException si el hotel no existe o no pertenece al tenant
     */
    private Hotel findHotelAndValidateTenant(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        // Validar multi-tenancy: el hotel debe pertenecer al tenant actual
        Long currentTenantId = TenantContext.getCurrentTenant();
        if (currentTenantId != null && !currentTenantId.equals(hotel.getId())) {
            log.warn("⚠️ Intento de acceso a hotel {} por tenant {}", id, currentTenantId);
            throw new HotelNotFoundException(id);
        }

        return hotel;
    }
}
