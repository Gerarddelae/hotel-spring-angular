package com.hotelsa.backend.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "hotelId", type = Long.class))
@Filter(name = "deletedFilter", condition = "deleted = :isDeleted")
@Filter(name = "tenantFilter", condition = "hotel_id = :hotelId")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
