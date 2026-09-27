package com.realestate.auth_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank @Email private String email;

    @NotBlank @Size(min = 6, max = 6) private String code;
}
