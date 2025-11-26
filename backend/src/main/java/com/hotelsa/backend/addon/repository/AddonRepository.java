package com.hotelsa.backend.addon.repository;

import com.hotelsa.backend.addon.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {

    // Búsqueda por texto parcial (case-insensitive)
    List<Addon> findByNameContainingIgnoreCase(String name);

    // Validación de duplicados por nombre (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Cargar múltiples addons por lista de IDs (útil para Booking)
    List<Addon> findByIdIn(List<Long> ids);

    // Listado ordenado por nombre ascendente
    List<Addon> findAllByOrderByNameAsc();

    // Retorna solo los addons activos
    List<Addon> findAllByActiveTrue();
}

