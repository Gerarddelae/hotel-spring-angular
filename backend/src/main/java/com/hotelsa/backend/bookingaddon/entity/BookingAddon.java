package com.hotelsa.backend.bookingaddon.entity;

import com.hotelsa.backend.addon.model.Addon;
import com.hotelsa.backend.booking.model.Booking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "booking_addon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
@Filter(name = "tenantFilter", condition = "hotel_id = :hotelId")
@SQLDelete(sql = "UPDATE booking_addon SET deleted = true WHERE booking_id = ? AND addon_id = ?")
public class BookingAddon {

    @EmbeddedId
    private BookingAddonId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("addonId")
    @JoinColumn(name = "addon_id", nullable = false)
    @ToString.Exclude
    private Addon addon;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @PrePersist
    @PreUpdate
    private void ensureHotelId() {
        if (this.hotelId == null && this.booking != null) {
            this.hotelId = this.booking.getHotelId();
        }
    }
}
