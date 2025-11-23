package com.hotelsa.backend.billaddon.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BillAddonId {

    @Column(name = "bill_id")
    private Long billId;

    @Column(name = "addon_id")
    private Long addonId;
}
