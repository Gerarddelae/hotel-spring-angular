package com.hotelsa.backend.guest.model;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE guests SET deleted = true WHERE id = ?")
public class Guest extends BaseEntity implements Serializable {

    // Relación para navegar al hotel, sin modificar la columna hotel_id (esta columna está en BaseEntity)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Hotel hotel;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String fullName;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String documentType;

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String documentNumber;

    @Email
    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String email;

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private String phone;

    @NotBlank
    @Size(max = 255)
    @Column(length = 255, nullable = false)
    private String address;

    @NotNull
    @Column(nullable = false)
    private Integer previousCancellations;

    @NotNull
    @Column(nullable = false)
    private Integer totalBookingsClient;
}
