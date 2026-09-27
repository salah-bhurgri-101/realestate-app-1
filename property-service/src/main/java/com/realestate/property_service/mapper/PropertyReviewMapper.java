package com.realestate.property_service.mapper;

import com.realestate.property_service.dto.request.PropertyReviewRequestDto;
import com.realestate.property_service.dto.response.PropertyReviewResponseDto;
import com.realestate.property_service.entity.PropertyReview;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PropertyReviewMapper {

    public PropertyReview toEntity(PropertyReviewRequestDto dto, UUID buyerId, UUID propertyId) {
        return PropertyReview.builder()
                .buyerId(buyerId)
                .propertyId(propertyId)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
    }

    public PropertyReviewResponseDto toResponseDto(PropertyReview review) {
        return PropertyReviewResponseDto.builder()
                .id(review.getId())
                .buyerId(review.getBuyerId())
                .propertyId(review.getPropertyId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
