package com.hotelsa.backend.room.model;

import com.hotelsa.backend.hotel.model.Hotel;
import com.hotelsa.backend.room.enums.RoomStatus;
import com.hotelsa.backend.room.enums.RoomType;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE rooms SET deleted = true WHERE id = ?")
@FilterDef(name = "roomDeletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@Filter(name = "roomDeletedFilter", condition = "deleted = :isDeleted")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type; // SINGLE, DOUBLE, SUITE...

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Double pricePerNight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status; // AVAILABLE, OCCUPIED, MAINTENANCE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    @ToString.Exclude
    private Hotel hotel;

    @Column(nullable = false)
    private boolean deleted = false;
}
