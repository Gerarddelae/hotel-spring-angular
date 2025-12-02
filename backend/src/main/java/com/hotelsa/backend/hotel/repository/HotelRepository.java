package com.hotelsa.backend.hotel.repository;

import com.hotelsa.backend.hotel.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    /**
     * Busca un hotel por su ID verificando que pertenezca al hotel especificado.
     * Útil para validación de multi-tenancy.
     *
     * @param id      ID del hotel a buscar
     * @param hotelId ID del hotel (tenant) para validación
     * @return Optional con el hotel si existe y pertenece al tenant
     */
    Optional<Hotel> findByIdAndId(Long id, Long hotelId);
}
