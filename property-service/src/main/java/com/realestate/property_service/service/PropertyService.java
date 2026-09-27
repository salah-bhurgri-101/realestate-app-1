package com.realestate.property_service.service;

import com.realestate.property_service.dto.request.*;
import com.realestate.property_service.dto.response.*;
import com.realestate.property_service.enums.AmenityType;
import java.util.List;
import java.util.UUID;

public interface PropertyService {

    PropertyResponseDto createProperty(PropertyRequestDto dto, UUID sellerId);

    PropertyResponseDto updateProperty(UUID propertyId, UUID sellerId, PropertyRequestDto dto);

    void deleteProperty(UUID propertyId, UUID sellerId);

    PropertyResponseDto getPropertyById(UUID propertyId, UUID viewerUserId, String viewerIp);

    PageResponseDto<PropertySummaryDto> searchProperties(PropertySearchFilterDto filter, int pageNo, int pageSize);

    PageResponseDto<PropertySummaryDto> getFeaturedProperties(int pageNo, int pageSize);

    PageResponseDto<PropertySummaryDto> getMyProperties(UUID sellerId, int pageNo, int pageSize);

    PropertyResponseDto updateStatus(UUID propertyId, PropertyStatusUpdateDto dto);

    PropertyResponseDto addImages(UUID propertyId, UUID sellerId, List<PropertyImageUploadRequestDto> images);

    void deleteImage(UUID propertyId, UUID imageId, UUID sellerId);

    PropertyResponseDto addAmenities(UUID propertyId, UUID sellerId, List<AmenityType> amenities);

    void removeAmenity(UUID propertyId, UUID sellerId, AmenityType amenity);
}
