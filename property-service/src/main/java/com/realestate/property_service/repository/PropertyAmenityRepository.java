package com.realestate.property_service.repository;

import com.realestate.property_service.entity.PropertyAmenity;
import com.realestate.property_service.enums.AmenityType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyAmenityRepository extends JpaRepository<PropertyAmenity, UUID> {

    List<PropertyAmenity> findByPropertyId(UUID propertyId);

    boolean existsByPropertyIdAndAmenity(UUID propertyId, AmenityType amenity);

    void deleteByPropertyId(UUID propertyId);

    void deleteByPropertyIdAndAmenity(UUID propertyId, AmenityType amenity);
}
