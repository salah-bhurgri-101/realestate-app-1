package com.realestate.property_service.service;

import com.realestate.property_service.dto.response.FavoriteResponseDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import java.util.UUID;

public interface FavoriteService {

    // returns true if the property was added to favorites, false if it was removed (toggle)
    boolean toggleFavorite(UUID buyerId, UUID propertyId);

    PageResponseDto<FavoriteResponseDto> getMyFavorites(UUID buyerId, int pageNo, int pageSize);
}
