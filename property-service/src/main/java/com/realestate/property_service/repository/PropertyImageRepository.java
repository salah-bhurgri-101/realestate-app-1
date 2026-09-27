package com.realestate.property_service.repository;

import com.realestate.property_service.entity.PropertyImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {

    List<PropertyImage> findByPropertyIdOrderBySortOrderAsc(UUID propertyId);

    Optional<PropertyImage> findByPropertyIdAndCoverImageTrue(UUID propertyId);

    void deleteByPropertyId(UUID propertyId);

    long countByPropertyId(UUID propertyId);
}
