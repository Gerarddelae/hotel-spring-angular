package com.hotelsa.backend.bookingaddon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookingAddonId {

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "addon_id")
    private Long addonId;
}
