package com.realestate.property_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    // populated only for validation failures (field name -> error message)
    private Map<String, String> validationErrors;
}
