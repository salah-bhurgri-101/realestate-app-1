package com.example.userservice.dto.request;

import com.example.userservice.domain.enums.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class AddressRequest {

    private AddressLabel label;

    @NotBlank @Size(max = 200) private String line1;

    @Size(max = 200) private String line2;

    @NotBlank @Size(max = 100) private String city;

    @Size(max = 100) private String state;

    @Size(max = 20) private String postalCode;

    @NotBlank @Size(min = 2, max = 2, message = "country must be an ISO 3166-1 alpha-2 code") private String country;

    private boolean isDefault;
}
