package com.hotelsa.backend.addon.model;

import com.hotelsa.backend.common.model.BaseEntity;
import com.hotelsa.backend.billaddon.entity.BillAddon;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE addons SET deleted = true WHERE id = ?")
public class Addon extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

    // Nuevo campo para activar/desactivar visualmente el addon
    @Column(name = "active", nullable = false)
    private boolean active = true;

    // Relación inversa hacia BookingAddon (lectura semántica)
    @OneToMany(mappedBy = "addon", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<com.hotelsa.backend.bookingaddon.entity.BookingAddon> bookings = new ArrayList<>();
}