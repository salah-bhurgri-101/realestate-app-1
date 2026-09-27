package com.realestate.property_service.mapper;

import com.realestate.property_service.dto.response.FavoriteResponseDto;
import com.realestate.property_service.dto.response.PropertySummaryDto;
import com.realestate.property_service.entity.Favorite;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {

    public FavoriteResponseDto toResponseDto(Favorite favorite, PropertySummaryDto propertySummary) {
        return FavoriteResponseDto.builder()
                .id(favorite.getId())
                .propertyId(favorite.getPropertyId())
                .property(propertySummary)
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
