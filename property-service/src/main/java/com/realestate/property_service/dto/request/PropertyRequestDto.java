package com.realestate.property_service.dto.request;

import com.realestate.property_service.enums.AmenityType;
import com.realestate.property_service.enums.PropertyType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyRequestDto {

    @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must not exceed 150 characters") private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters") private String description;

    @NotNull(message = "Property type is required") private PropertyType propertyType;

    @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") private BigDecimal price;

    @Positive(message = "Area must be a positive number") private Double area;

    @PositiveOrZero(message = "Bedrooms cannot be negative") private Integer bedrooms;

    @PositiveOrZero(message = "Bathrooms cannot be negative") private Integer bathrooms;

    @PositiveOrZero(message = "Floors cannot be negative") private Integer floors;

    @NotBlank(message = "City is required") private String city;

    private String areaName;

    private String address;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90") @DecimalMax(value = "90.0", message = "Latitude must be <= 90") private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180") @DecimalMax(value = "180.0", message = "Longitude must be <= 180") private Double longitude;

    private List<AmenityType> amenities;
}
