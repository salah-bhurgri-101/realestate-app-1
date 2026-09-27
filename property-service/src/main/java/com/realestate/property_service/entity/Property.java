package com.realestate.property_service.entity;

import com.realestate.property_service.enums.PropertyStatus;
import com.realestate.property_service.enums.PropertyType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "properties",
        indexes = {
            @Index(name = "idx_property_city", columnList = "city"),
            @Index(name = "idx_property_status", columnList = "status"),
            @Index(name = "idx_property_type", columnList = "propertyType"),
            @Index(name = "idx_property_seller", columnList = "sellerId")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID sellerId;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Column(nullable = false)
    private BigDecimal price;

    private Double area;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer floors;

    @Column(nullable = false)
    private String city;

    private String areaName;

    private String address;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    private PropertyStatus status;

    private Boolean featured;

    private Boolean active;

    private Long totalViews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PropertyImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PropertyAmenity> amenities = new ArrayList<>();

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) status = PropertyStatus.PENDING;

        if (active == null) active = true;

        if (featured == null) featured = false;

        if (totalViews == null) totalViews = 0L;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
