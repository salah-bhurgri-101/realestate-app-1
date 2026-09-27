package com.realestate.property_service.dto.response;

import com.realestate.property_service.enums.AmenityType;
import com.realestate.property_service.enums.PropertyStatus;
import com.realestate.property_service.enums.PropertyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponseDto {

    private UUID id;

    private UUID sellerId;

    private String title;

    private String description;

    private PropertyType propertyType;

    private BigDecimal price;

    private Double area;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer floors;

    private String city;

    private String areaName;

    private String address;

    private Double latitude;

    private Double longitude;

    private PropertyStatus status;

    private Boolean featured;

    private Boolean active;

    private Long totalViews;

    private Double averageRating;

    private Long reviewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<PropertyImageDto> images;

    private List<AmenityType> amenities;
}
