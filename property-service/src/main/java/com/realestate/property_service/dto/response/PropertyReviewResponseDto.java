package com.realestate.property_service.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyReviewResponseDto {

    private UUID id;

    private UUID buyerId;

    private UUID propertyId;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
}
