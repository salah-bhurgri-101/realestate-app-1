package com.realestate.property_service.repository;

import com.realestate.property_service.entity.PropertyReview;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyReviewRepository extends JpaRepository<PropertyReview, UUID> {

    Page<PropertyReview> findByPropertyId(UUID propertyId, Pageable pageable);

    Optional<PropertyReview> findByBuyerIdAndPropertyId(UUID buyerId, UUID propertyId);

    boolean existsByBuyerIdAndPropertyId(UUID buyerId, UUID propertyId);

    @Query("SELECT AVG(r.rating) FROM PropertyReview r WHERE r.propertyId = :propertyId")
    Double findAverageRatingByPropertyId(@Param("propertyId") UUID propertyId);

    long countByPropertyId(UUID propertyId);
}
