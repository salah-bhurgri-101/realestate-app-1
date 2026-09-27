package com.realestate.property_service.service.impl;

import com.example.auditstarter.AuditClient;
import com.example.auditstarter.annotation.AuditLog;
import com.example.auditstarter.model.AuditAction;
import com.realestate.property_service.dto.request.*;
import com.realestate.property_service.dto.response.*;
import com.realestate.property_service.entity.Property;
import com.realestate.property_service.entity.PropertyAmenity;
import com.realestate.property_service.entity.PropertyImage;
import com.realestate.property_service.entity.PropertyView;
import com.realestate.property_service.enums.AmenityType;
import com.realestate.property_service.enums.PropertyStatus;
import com.realestate.property_service.exception.ResourceNotFoundException;
import com.realestate.property_service.exception.UnauthorizedActionException;
import com.realestate.property_service.mapper.PropertyMapper;
import com.realestate.property_service.repository.*;
import com.realestate.property_service.service.PropertyService;
import com.realestate.property_service.specification.PropertySpecification;
import com.realestate.property_service.util.PaginationUtils;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final AuditClient auditClient;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "price", "area", "totalViews");

    private static final int VIEW_DEDUP_WINDOW_MINUTES = 30;

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final PropertyAmenityRepository propertyAmenityRepository;
    private final PropertyReviewRepository propertyReviewRepository;
    private final PropertyViewRepository propertyViewRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.CREATE,
            entityType = "Property",
            entityIdExpression = "#sellerId",
            description = "Seller created a property")
    public PropertyResponseDto createProperty(PropertyRequestDto dto, UUID sellerId) {
        Property property = propertyMapper.toEntity(dto);
        property.setSellerId(sellerId);

        if (dto.getAmenities() != null) {
            List<PropertyAmenity> amenities = dto.getAmenities().stream()
                    .distinct()
                    .map(type -> PropertyAmenity.builder()
                            .amenity(type)
                            .property(property)
                            .build())
                    .collect(Collectors.toList());
            property.getAmenities().addAll(amenities);
        }

        Property saved = propertyRepository.save(property);

        //        AuditEvent auditEvent = AuditEvent.builder()
        //                .action(AuditAction.CREATE)
        //                .traceId("dasda")
        //                .serviceName("Property Service")
        //                .build();
        //
        //        auditClient.log(auditEvent);

        return propertyMapper.toResponseDto(saved, null, 0L);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.UPDATE,
            entityType = "Property",
            entityIdExpression = "#propertyId",
            description = "Property updated")
    public PropertyResponseDto updateProperty(UUID propertyId, UUID sellerId, PropertyRequestDto dto) {
        Property property = getOwnedPropertyOrThrow(propertyId, sellerId);

        propertyMapper.updateEntityFromDto(property, dto);

        // (It have issue of uniqe) Replace amenities wholesale (simplest correct approach with orphanRemoval=true)
        //        property.getAmenities().clear();
        //        if (dto.getAmenities() != null) {
        //            List<PropertyAmenity> amenities = dto.getAmenities().stream()
        //                    .distinct()
        //                    .map(type -> PropertyAmenity.builder()
        //                            .amenity(type)
        //                            .property(property)
        //                            .build())
        //                    .collect(Collectors.toList());
        //            property.getAmenities().addAll(amenities);
        //        }

        //      obove code replace this it have not any issue
        if (dto.getAmenities() != null) {
            Set<AmenityType> newAmenities = new HashSet<>(dto.getAmenities());

            Set<AmenityType> currentAmenities = property.getAmenities().stream()
                    .map(PropertyAmenity::getAmenity)
                    .collect(Collectors.toSet());

            // Remove amenities that are no longer present
            property.getAmenities().removeIf(pa -> !newAmenities.contains(pa.getAmenity()));

            // Add only the amenities that are genuinely new
            newAmenities.stream()
                    .filter(a -> !currentAmenities.contains(a))
                    .map(type -> PropertyAmenity.builder()
                            .amenity(type)
                            .property(property)
                            .build())
                    .forEach(property.getAmenities()::add);
        } else {
            property.getAmenities().clear();
        }

        Property saved = propertyRepository.save(property);
        return toResponseDtoWithRating(saved);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.DELETE,
            entityType = "Property",
            entityIdExpression = "#propertyId",
            description = "Property deleted")
    public void deleteProperty(UUID propertyId, UUID sellerId) {
        Property property = getOwnedPropertyOrThrow(propertyId, sellerId);
        propertyRepository.delete(property);
    }

    /*@Override
    @Transactional
    public PropertyResponseDto getPropertyById(UUID propertyId, UUID viewerUserId, String viewerIp) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        recordViewIfNotDuplicate(propertyId, viewerUserId, viewerIp);

        return toResponseDtoWithRating(property);
    }*/
    @Override
    @Transactional
    public PropertyResponseDto getPropertyById(UUID propertyId, UUID viewerUserId, String viewerIp) {
        // View tracking must run on every call, so it stays outside the cached method.
        recordViewIfNotDuplicate(propertyId, viewerUserId, viewerIp);
        return getCachedPropertyWithRating(propertyId);
    }

    public PropertyResponseDto getCachedPropertyWithRating(UUID propertyId) {
        Property property = propertyRepository
                .findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));
        return toResponseDtoWithRating(property);
    }

    @Override
    public PageResponseDto<PropertySummaryDto> searchProperties(
            PropertySearchFilterDto filter, int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize, resolveSort(filter));

        Page<Property> page = propertyRepository.findAll(PropertySpecification.build(filter), pageable);

        //        propertyRepository.findAllPro(pageable)  Set IN property entity to LIst<Amenities> to Set<Amenities>

        List<PropertySummaryDto> content =
                page.getContent().stream().map(propertyMapper::toSummaryDto).collect(Collectors.toList());

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<PropertySummaryDto> getFeaturedProperties(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Property> page =
                propertyRepository.findByFeaturedTrueAndActiveTrueAndStatus(PropertyStatus.APPROVED, pageable);

        List<PropertySummaryDto> content =
                page.getContent().stream().map(propertyMapper::toSummaryDto).collect(Collectors.toList());

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    public PageResponseDto<PropertySummaryDto> getMyProperties(UUID sellerId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Property> page = propertyRepository.findBySellerId(sellerId, pageable);

        List<PropertySummaryDto> content =
                page.getContent().stream().map(propertyMapper::toSummaryDto).collect(Collectors.toList());

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.UPDATE,
            entityType = "Property",
            entityIdExpression = "#propertyId",
            description = "Property status changed")
    public PropertyResponseDto updateStatus(UUID propertyId, PropertyStatusUpdateDto dto) {
        Property property = propertyRepository
                .findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        property.setStatus(dto.getStatus());
        Property saved = propertyRepository.save(property);
        return toResponseDtoWithRating(saved);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.CREATE,
            entityType = "PropertyImage",
            entityIdExpression = "#propertyId",
            description = "Images added")
    public PropertyResponseDto addImages(UUID propertyId, UUID sellerId, List<PropertyImageUploadRequestDto> images) {

        Property property = getOwnedPropertyOrThrow(propertyId, sellerId);

        boolean incomingHasCover = images.stream().anyMatch(img -> Boolean.TRUE.equals(img.getCoverImage()));

        if (incomingHasCover) {
            // unset any existing cover so there's only ever one
            property.getImages().forEach(img -> img.setCoverImage(false));
        }

        List<PropertyImage> newImages = images.stream()
                .map(dto -> PropertyImage.builder()
                        .imageUrl(dto.getImageUrl())
                        .fileName(dto.getFileName())
                        .fileSize(dto.getFileSize())
                        .coverImage(Boolean.TRUE.equals(dto.getCoverImage()))
                        .sortOrder(dto.getSortOrder())
                        .property(property)
                        .build())
                .collect(Collectors.toList());

        property.getImages().addAll(newImages);

        Property saved = propertyRepository.save(property);
        return toResponseDtoWithRating(saved);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.DELETE,
            entityType = "PropertyImage",
            entityIdExpression = "#propertyId",
            description = "Image deleted")
    public void deleteImage(UUID propertyId, UUID imageId, UUID sellerId) {
        Property property = getOwnedPropertyOrThrow(propertyId, sellerId);

        PropertyImage image = propertyImageRepository
                .findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        if (!image.getProperty().getId().equals(property.getId())) {
            throw new UnauthorizedActionException("Image does not belong to this property");
        }

        property.getImages().remove(image);
        propertyImageRepository.delete(image);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.CREATE,
            entityType = "PropertyAmenity",
            entityIdExpression = "#propertyId",
            description = "Amenities added")
    public PropertyResponseDto addAmenities(UUID propertyId, UUID sellerId, List<AmenityType> amenities) {
        Property property = getOwnedPropertyOrThrow(propertyId, sellerId);

        List<PropertyAmenity> toAdd = amenities.stream()
                .distinct()
                .filter(type -> !propertyAmenityRepository.existsByPropertyIdAndAmenity(propertyId, type))
                .map(type -> PropertyAmenity.builder()
                        .amenity(type)
                        .property(property)
                        .build())
                .collect(Collectors.toList());

        property.getAmenities().addAll(toAdd);

        Property saved = propertyRepository.save(property);
        return toResponseDtoWithRating(saved);
    }

    @Override
    @Transactional
    @AuditLog(
            action = AuditAction.DELETE,
            entityType = "PropertyAmenity",
            entityIdExpression = "#propertyId",
            description = "Amenity removed")
    public void removeAmenity(UUID propertyId, UUID sellerId, AmenityType amenity) {
        getOwnedPropertyOrThrow(propertyId, sellerId);
        propertyAmenityRepository.deleteByPropertyIdAndAmenity(propertyId, amenity);
    }

    // ---- helpers ----

    private Property getOwnedPropertyOrThrow(UUID propertyId, UUID sellerId) {
        Property property = propertyRepository
                .findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        if (!property.getSellerId().equals(sellerId)) {
            throw new UnauthorizedActionException("You do not own this property");
        }
        return property;
    }

    private PropertyResponseDto toResponseDtoWithRating(Property property) {
        Double avgRating = propertyReviewRepository.findAverageRatingByPropertyId(property.getId());
        long reviewCount = propertyReviewRepository.countByPropertyId(property.getId());
        return propertyMapper.toResponseDto(property, avgRating, reviewCount);
    }

    private void recordViewIfNotDuplicate(UUID propertyId, UUID viewerUserId, String viewerIp) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(VIEW_DEDUP_WINDOW_MINUTES);

        boolean alreadyCounted = viewerUserId != null
                ? propertyViewRepository.existsByPropertyIdAndUserIdAndViewedAtAfter(propertyId, viewerUserId, since)
                : propertyViewRepository.existsByPropertyIdAndIpAddressAndViewedAtAfter(propertyId, viewerIp, since);

        if (alreadyCounted) {
            return;
        }

        PropertyView view = PropertyView.builder()
                .propertyId(propertyId)
                .userId(viewerUserId)
                .ipAddress(viewerIp)
                .build();
        propertyViewRepository.save(view);

        propertyRepository.incrementViewCount(propertyId);
    }

    private Sort resolveSort(PropertySearchFilterDto filter) {
        String sortBy = ALLOWED_SORT_FIELDS.contains(filter.getSortBy()) ? filter.getSortBy() : "createdAt";
        Sort.Direction direction =
                "ASC".equalsIgnoreCase(filter.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortBy);
    }
}
