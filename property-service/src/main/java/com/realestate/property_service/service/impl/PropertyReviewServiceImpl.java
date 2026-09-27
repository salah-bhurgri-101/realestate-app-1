package com.realestate.property_service.service.impl;

import com.realestate.property_service.dto.request.PropertyReviewRequestDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import com.realestate.property_service.dto.response.PropertyReviewResponseDto;
import com.realestate.property_service.entity.PropertyReview;
import com.realestate.property_service.exception.DuplicateResourceException;
import com.realestate.property_service.exception.ResourceNotFoundException;
import com.realestate.property_service.exception.UnauthorizedActionException;
import com.realestate.property_service.mapper.PropertyReviewMapper;
import com.realestate.property_service.repository.PropertyRepository;
import com.realestate.property_service.repository.PropertyReviewRepository;
import com.realestate.property_service.service.PropertyReviewService;
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
public class PropertyReviewServiceImpl implements PropertyReviewService {

    private final PropertyReviewRepository propertyReviewRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyReviewMapper propertyReviewMapper;

    @Override
    @Transactional
    public PropertyReviewResponseDto addReview(UUID propertyId, UUID buyerId, PropertyReviewRequestDto dto) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResourceNotFoundException("Property not found: " + propertyId);
        }

        /*
         * Here also check buyerId and sellerId Is not same.
         * if Same Seller does not add review Own Property. not add rating,comment own proeprty
         * seller it may add review other seller property or add rating, comment */

        if (propertyReviewRepository.existsByBuyerIdAndPropertyId(buyerId, propertyId)) {
            throw new DuplicateResourceException("You have already reviewed this property");
        }

        PropertyReview review = propertyReviewMapper.toEntity(dto, buyerId, propertyId);
        PropertyReview saved = propertyReviewRepository.save(review);
        return propertyReviewMapper.toResponseDto(saved);
    }

    @Override
    public PageResponseDto<PropertyReviewResponseDto> getReviews(UUID propertyId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PropertyReview> page = propertyReviewRepository.findByPropertyId(propertyId, pageable);

        List<PropertyReviewResponseDto> content = page.getContent().stream()
                .map(propertyReviewMapper::toResponseDto)
                .collect(Collectors.toList());

        return PaginationUtils.toPageResponse(page, content);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID buyerId) {
        PropertyReview review = propertyReviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getBuyerId().equals(buyerId)) {
            throw new UnauthorizedActionException("You can only delete your own review");
        }

        propertyReviewRepository.delete(review);
    }
}
