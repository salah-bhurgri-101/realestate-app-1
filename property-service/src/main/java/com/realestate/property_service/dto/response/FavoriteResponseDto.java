package com.realestate.property_service.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponseDto {

    private UUID id;

    private UUID propertyId;

    // lightweight property info so frontend doesn't need a second call
    private PropertySummaryDto property;

    private LocalDateTime createdAt;
}
