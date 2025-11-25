package com.hotelsa.backend.guest.repository;

import com.hotelsa.backend.guest.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    // Comprueba existencia por número de documento
    boolean existsByDocumentNumber(String documentNumber);

    // Buscar huésped por correo electrónico
    Optional<Guest> findByEmail(String email);

    // Buscar por nombre (búsqueda parcial, case-insensitive)
    List<Guest> findByFullNameContainingIgnoreCase(String fullName);

    @Query("""
            SELECT g FROM Guest g
            WHERE (:query IS NULL OR :query = '' OR
                  LOWER(g.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(g.documentNumber) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(g.email) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    List<Guest> search(@Param("query") String query);
}
