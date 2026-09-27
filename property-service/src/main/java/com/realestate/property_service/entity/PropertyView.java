package com.realestate.property_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "property_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID propertyId;

    private UUID userId;

    private String ipAddress;

    private LocalDateTime viewedAt;

    @PrePersist
    void prePersist() {
        viewedAt = LocalDateTime.now();
    }
}
