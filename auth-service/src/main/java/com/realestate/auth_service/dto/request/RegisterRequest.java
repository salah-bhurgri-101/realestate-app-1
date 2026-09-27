package com.realestate.auth_service.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Email private String email;

    @NotBlank @Size(min = 8, max = 100) private String password;

    @NotBlank @Size(min = 2, max = 100) private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number") private String phone; // optional

    @Size(max = 2000) private String bio;

    @Size(max = 512) private String avatarUrl;

    @Size(max = 512) private String coverUrl;

    @Past private LocalDate dateOfBirth;

    @Size(max = 20) private String gender;
}
