package com.realestate.property_service.mapper;

import com.realestate.property_service.dto.request.*;
import com.realestate.property_service.dto.response.*;
import com.realestate.property_service.entity.Property;
import com.realestate.property_service.entity.PropertyAmenity;
import com.realestate.property_service.entity.PropertyImage;
import com.realestate.property_service.enums.AmenityType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {

    public Property toEntity(PropertyRequestDto dto) {
        return Property.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .propertyType(dto.getPropertyType())
                .price(dto.getPrice())
                .area(dto.getArea())
                .bedrooms(dto.getBedrooms())
                .bathrooms(dto.getBathrooms())
                .floors(dto.getFloors())
                .city(dto.getCity())
                .areaName(dto.getAreaName())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    // Mutates an existing managed entity in-place (used for PUT-style update)
    public void updateEntityFromDto(Property property, PropertyRequestDto dto) {
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setPropertyType(dto.getPropertyType());
        property.setPrice(dto.getPrice());
        property.setArea(dto.getArea());
        property.setBedrooms(dto.getBedrooms());
        property.setBathrooms(dto.getBathrooms());
        property.setFloors(dto.getFloors());
        property.setCity(dto.getCity());
        property.setAreaName(dto.getAreaName());
        property.setAddress(dto.getAddress());
        property.setLatitude(dto.getLatitude());
        property.setLongitude(dto.getLongitude());
    }

    public PropertyResponseDto toResponseDto(Property property, Double averageRating, Long reviewCount) {
        return PropertyResponseDto.builder()
                .id(property.getId())
                .sellerId(property.getSellerId())
                .title(property.getTitle())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .price(property.getPrice())
                .area(property.getArea())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .floors(property.getFloors())
                .city(property.getCity())
                .areaName(property.getAreaName())
                .address(property.getAddress())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .status(property.getStatus())
                .featured(property.getFeatured())
                .active(property.getActive())
                .totalViews(property.getTotalViews())
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .images(toImageDtoList(property.getImages()))
                .amenities(toAmenityTypeList(property.getAmenities()))
                .build();
    }

    public PropertySummaryDto toSummaryDto(Property property) {
        return PropertySummaryDto.builder()
                .id(property.getId())
                .title(property.getTitle())
                .propertyType(property.getPropertyType())
                .price(property.getPrice())
                .area(property.getArea())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .city(property.getCity())
                .areaName(property.getAreaName())
                .coverImageUrl(resolveCoverImageUrl(property.getImages()))
                .status(property.getStatus())
                .featured(property.getFeatured())
                .createdAt(property.getCreatedAt())
                .build();
    }

    public PropertyImageDto toImageDto(PropertyImage image) {
        return PropertyImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .coverImage(image.getCoverImage())
                .fileName(image.getFileName())
                .fileSize(image.getFileSize())
                .sortOrder(image.getSortOrder())
                .build();
    }

    public List<PropertyImageDto> toImageDtoList(List<PropertyImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .sorted(Comparator.comparing(
                        PropertyImage::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toImageDto)
                .collect(Collectors.toList());
    }

    public List<AmenityType> toAmenityTypeList(List<PropertyAmenity> amenities) {
        if (amenities == null) return List.of();
        return amenities.stream().map(PropertyAmenity::getAmenity).collect(Collectors.toList());
    }

    private String resolveCoverImageUrl(List<PropertyImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getCoverImage()))
                .findFirst()
                .map(PropertyImage::getImageUrl)
                .orElse(images.get(0).getImageUrl());
    }
}
