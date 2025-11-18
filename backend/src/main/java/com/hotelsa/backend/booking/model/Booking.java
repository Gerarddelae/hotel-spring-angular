package com.hotelsa.backend.booking.model;

import com.hotelsa.backend.booking.enums.BookingStatus;
import com.hotelsa.backend.guest.model.Guest;
import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.room.model.Room;
import com.hotelsa.backend.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE bookings SET deleted = true WHERE id = ?")
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_guest_fk", nullable = false)
    @ToString.Exclude
    private Guest guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_room_fk", nullable = false)
    @ToString.Exclude
    private Room room;

    @NotNull
    @Column(name = "checkInDate", nullable = false)
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "checkOutDate", nullable = false)
    private LocalDate checkOutDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private BookingStatus status;

    @NotBlank
    @Size(max = 100)
    @Column(name = "createdBy", length = 100, nullable = false)
    private String createdBy;

    @NotNull
    @Column(name = "bookingLeadTime", nullable = false)
    private LocalDate bookingLeadTime;

    @Column(name = "notes", length = 255)
    private String notes;

    // Relación con BookingAddon (no orphanRemoval porque se usa soft-delete en BookingAddon)
    @OneToMany(mappedBy = "booking", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = false)
    @ToString.Exclude
    private List<com.hotelsa.backend.bookingaddon.entity.BookingAddon> addons = new ArrayList<>();
}
