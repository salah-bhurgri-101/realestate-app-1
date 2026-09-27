package com.realestate.property_service.service.impl;

import com.realestate.property_service.dto.response.FavoriteResponseDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import com.realestate.property_service.dto.response.PropertySummaryDto;
import com.realestate.property_service.entity.Favorite;
import com.realestate.property_service.exception.ResourceNotFoundException;
import com.realestate.property_service.mapper.FavoriteMapper;
import com.realestate.property_service.mapper.PropertyMapper;
import com.realestate.property_service.repository.FavoriteRepository;
import com.realestate.property_service.repository.PropertyRepository;
import com.realestate.property_service.service.FavoriteService;
import com.realestate.property_service.util.PaginationUtils;
import java.util.List;
import java.util.UUID;
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
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public boolean toggleFavorite(UUID buyerId, UUID propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found: " + propertyId);
        }

        /*Check BuyerId Is Same or not same SellerId
         * if we not check it sellerId favorities or like active is work
         * seller does not,favorites own proeprty. It like Other Seller Property*/

        if (favoriteRepository.existsByBuyerIdAndPropertyId(buyerId, propertyId)) {
            favoriteRepository.deleteByBuyerIdAndPropertyId(buyerId, propertyId);
            return false;
        }

        Favorite favorite =
                Favorite.builder().buyerId(buyerId).propertyId(propertyId).build();
        favoriteRepository.save(favorite);
        return true;
    }

    @Override
    public PageResponseDto<FavoriteResponseDto> getMyFavorites(UUID buyerId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> page = favoriteRepository.findByBuyerId(buyerId, pageable);

        List<FavoriteResponseDto> content = page.getContent().stream()
                .map(favorite -> {
                    PropertySummaryDto summary = propertyRepository
                            .findById(favorite.getPropertyId())
                            .map(propertyMapper::toSummaryDto)
                            .orElse(null); // property may have been deleted since favoriting
                    return favoriteMapper.toResponseDto(favorite, summary);
                })
                .collect(Collectors.toList());

        return PaginationUtils.toPageResponse(page, content);
    }
}
