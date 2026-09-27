package com.realestate.auth_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @NotBlank @Email private String email;
}
