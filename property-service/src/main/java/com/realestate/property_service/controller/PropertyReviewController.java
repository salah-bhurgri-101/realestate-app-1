package com.realestate.property_service.controller;

import com.realestate.property_service.dto.request.PropertyReviewRequestDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import com.realestate.property_service.dto.response.PropertyReviewResponseDto;
import com.realestate.property_service.service.PropertyReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PropertyReviewController {

    private final PropertyReviewService propertyReviewService;

    @PostMapping("/api/properties/{propertyId}/reviews")
    public ResponseEntity<PropertyReviewResponseDto> addReview(
            @AuthenticationPrincipal UUID buyerId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody PropertyReviewRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyReviewService.addReview(propertyId, buyerId, dto));
    }

    @GetMapping("/api/properties/{propertyId}/reviews")
    public ResponseEntity<PageResponseDto<PropertyReviewResponseDto>> getReviews(
            @PathVariable UUID propertyId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(propertyReviewService.getReviews(propertyId, pageNo, pageSize));
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal UUID buyerId, @PathVariable UUID reviewId) {
        propertyReviewService.deleteReview(reviewId, buyerId);
        return ResponseEntity.noContent().build();
    }
}
