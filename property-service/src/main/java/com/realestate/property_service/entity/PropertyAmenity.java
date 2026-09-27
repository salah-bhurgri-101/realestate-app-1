package com.realestate.property_service.entity;

import com.realestate.property_service.enums.AmenityType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "property_amenities",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_property_amenity",
                    columnNames = {"property_id", "amenity"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmenityType amenity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}
