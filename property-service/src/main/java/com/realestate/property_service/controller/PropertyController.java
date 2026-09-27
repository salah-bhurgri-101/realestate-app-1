package com.realestate.property_service.controller;

import com.realestate.property_service.dto.request.*;
import com.realestate.property_service.dto.response.*;
import com.realestate.property_service.enums.AmenityType;
import com.realestate.property_service.service.PropertyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyResponseDto> createProperty(
            @AuthenticationPrincipal UUID sellerId, @Valid @RequestBody PropertyRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(dto, sellerId));
    }

    //    It has Issue unique amenities.   Solve the problem. correct It then remove comment

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyResponseDto> updateProperty(
            @AuthenticationPrincipal UUID sellerId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody PropertyRequestDto dto) {
        return ResponseEntity.ok(propertyService.updateProperty(propertyId, sellerId, dto));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(@AuthenticationPrincipal UUID sellerId, @PathVariable UUID propertyId) {
        propertyService.deleteProperty(propertyId, sellerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyResponseDto> getProperty(
            @PathVariable UUID propertyId, @AuthenticationPrincipal UUID viewerUserId, HttpServletRequest request) {
        String viewerIp = resolveClientIp(request);
        return ResponseEntity.ok(propertyService.getPropertyById(propertyId, viewerUserId, viewerIp));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<PropertySummaryDto>> searchProperties(
            @ModelAttribute PropertySearchFilterDto filter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(propertyService.searchProperties(filter, pageNo, pageSize));
    }

    @GetMapping("/featured")
    public ResponseEntity<PageResponseDto<PropertySummaryDto>> getFeaturedProperties(
            @RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "3") int pageSize) {
        return ResponseEntity.ok(propertyService.getFeaturedProperties(pageNo, pageSize));
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponseDto<PropertySummaryDto>> getMyProperties(
            @AuthenticationPrincipal UUID sellerId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(propertyService.getMyProperties(sellerId, pageNo, pageSize));
    }

    @PatchMapping("/{propertyId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyResponseDto> updateStatus(
            @PathVariable UUID propertyId, @Valid @RequestBody PropertyStatusUpdateDto dto) {
        return ResponseEntity.ok(propertyService.updateStatus(propertyId, dto));
    }

    @PostMapping("/{propertyId}/images")
    public ResponseEntity<PropertyResponseDto> addImages(
            @AuthenticationPrincipal UUID sellerId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody List<PropertyImageUploadRequestDto> images) {
        return ResponseEntity.ok(propertyService.addImages(propertyId, sellerId, images));
    }

    @DeleteMapping("/{propertyId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @AuthenticationPrincipal UUID sellerId, @PathVariable UUID propertyId, @PathVariable UUID imageId) {
        propertyService.deleteImage(propertyId, imageId, sellerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{propertyId}/amenities")
    public ResponseEntity<PropertyResponseDto> addAmenities(
            @AuthenticationPrincipal UUID sellerId,
            @PathVariable UUID propertyId,
            @RequestBody List<AmenityType> amenities) {
        return ResponseEntity.ok(propertyService.addAmenities(propertyId, sellerId, amenities));
    }

    @DeleteMapping("/{propertyId}/amenities/{amenity}")
    public ResponseEntity<Void> removeAmenity(
            @AuthenticationPrincipal UUID sellerId, @PathVariable UUID propertyId, @PathVariable AmenityType amenity) {
        propertyService.removeAmenity(propertyId, sellerId, amenity);
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
