package com.realestate.property_service.repository;

import com.realestate.property_service.entity.PropertyView;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyViewRepository extends JpaRepository<PropertyView, UUID> {

    long countByPropertyId(UUID propertyId);

    // Used to avoid counting repeated views from the same logged-in user within a short window
    boolean existsByPropertyIdAndUserIdAndViewedAtAfter(UUID propertyId, UUID userId, LocalDateTime since);

    // Used for anonymous (non-logged-in) visitors, dedup by IP
    boolean existsByPropertyIdAndIpAddressAndViewedAtAfter(UUID propertyId, String ipAddress, LocalDateTime since);
}
