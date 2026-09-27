package com.realestate.property_service.dto.response;

import com.realestate.property_service.enums.PropertyStatus;
import com.realestate.property_service.enums.PropertyType;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertySearchFilterDto {

    private String city;

    private PropertyType propertyType;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private Integer minBedrooms;

    private Integer maxBedrooms;

    private Double minArea;

    private Double maxArea;

    private PropertyStatus status;

    private Boolean featured;

    // free-text search across title/description
    private String keyword;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";
}
