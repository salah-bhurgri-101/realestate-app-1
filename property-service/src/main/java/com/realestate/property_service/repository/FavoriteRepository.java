package com.realestate.property_service.repository;

import com.realestate.property_service.entity.Favorite;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    boolean existsByBuyerIdAndPropertyId(UUID buyerId, UUID propertyId);

    Optional<Favorite> findByBuyerIdAndPropertyId(UUID buyerId, UUID propertyId);

    Page<Favorite> findByBuyerId(UUID buyerId, Pageable pageable);

    void deleteByBuyerIdAndPropertyId(UUID buyerId, UUID propertyId);

    long countByPropertyId(UUID propertyId);
}
