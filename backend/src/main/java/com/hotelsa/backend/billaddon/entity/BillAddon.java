package com.hotelsa.backend.billaddon.entity;

import com.hotelsa.backend.bill.model.Bill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_addon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
@Filter(name = "tenantFilter", condition = "hotel_id = :hotelId")
@SQLDelete(sql = "UPDATE bill_addon SET deleted = true WHERE bill_id = ? AND addon_id = ?")
public class BillAddon {

    @EmbeddedId
    private BillAddonId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("billId")
    @JoinColumn(name = "bill_id", nullable = false)
    @ToString.Exclude
    private Bill bill;

    @Column(name = "addon_id", insertable = false, updatable = false)
    private Long addonId;

    @Column(name = "addon_name")
    private String addonName;

    @Column(name = "addon_description")
    private String addonDescription;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        if (this.quantity == null || this.quantity < 1) {
            this.quantity = 1;
        }
        if (this.unitPrice != null) {
            this.totalPrice = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
        if (this.hotelId == null && this.bill != null) {
            this.hotelId = this.bill.getHotelId();
        }
    }
}
