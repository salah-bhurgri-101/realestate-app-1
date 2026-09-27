package com.realestate.property_service.dto.request;

import com.realestate.property_service.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyStatusUpdateDto {

    @NotNull(message = "Status is required") private PropertyStatus status;

    // optional, useful when status = REJECTED
    private String reason;
}
