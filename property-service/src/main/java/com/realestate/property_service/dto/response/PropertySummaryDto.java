package com.realestate.property_service.dto.response;

import com.realestate.property_service.enums.PropertyStatus;
import com.realestate.property_service.enums.PropertyType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySummaryDto {

    private UUID id;

    private String title;

    private PropertyType propertyType;

    private BigDecimal price;

    private Double area;

    private Integer bedrooms;

    private Integer bathrooms;

    private String city;

    private String areaName;

    private String coverImageUrl;

    private PropertyStatus status;

    private Boolean featured;

    private LocalDateTime createdAt;
}
