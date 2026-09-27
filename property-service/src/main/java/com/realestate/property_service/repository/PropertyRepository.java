package com.realestate.property_service.repository;

import com.realestate.property_service.entity.Property;
import com.realestate.property_service.enums.PropertyStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {

    // Used for ownership checks before update/delete in service layer
    Optional<Property> findByIdAndSellerId(UUID id, UUID sellerId);

    boolean existsByIdAndSellerId(UUID id, UUID sellerId);

    Page<Property> findBySellerId(UUID sellerId, Pageable pageable);

    Page<Property> findByStatusAndActiveTrue(PropertyStatus status, Pageable pageable);

    Page<Property> findByFeaturedTrueAndActiveTrueAndStatus(PropertyStatus status, Pageable pageable);

    List<Property> findByCityAndActiveTrueAndStatus(String city, PropertyStatus status);

    @Modifying
    @Query("UPDATE Property p SET p.totalViews = p.totalViews + 1 WHERE p.id = :propertyId")
    void incrementViewCount(@Param("propertyId") UUID propertyId);

    long countBySellerId(UUID sellerId);

    @Query("select p from Property p left join fetch p.images left join fetch p.amenities")
    Page<Property> findAllPro(Pageable pageable);
}
