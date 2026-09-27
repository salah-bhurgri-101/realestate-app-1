package com.example.userservice.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpsertProfileRequest {

    @Size(max = 100) private String displayName;

    @Size(max = 2000) private String bio;

    @Size(max = 512) private String avatarUrl;

    @Size(max = 512) private String coverUrl;

    @Past private LocalDate dateOfBirth;

    @Size(max = 20) private String gender;

    @Size(max = 20) private String phone;
}
