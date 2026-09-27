package com.realestate.property_service.service;

import com.realestate.property_service.dto.request.PropertyReviewRequestDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import com.realestate.property_service.dto.response.PropertyReviewResponseDto;
import java.util.UUID;

public interface PropertyReviewService {

    PropertyReviewResponseDto addReview(UUID propertyId, UUID buyerId, PropertyReviewRequestDto dto);

    PageResponseDto<PropertyReviewResponseDto> getReviews(UUID propertyId, int pageNo, int pageSize);

    void deleteReview(UUID reviewId, UUID buyerId);
}
