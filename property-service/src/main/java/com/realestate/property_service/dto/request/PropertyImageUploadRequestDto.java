package com.realestate.property_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImageUploadRequestDto {

    @NotBlank(message = "Image URL is required") private String imageUrl;

    private String fileName;

    private Long fileSize;

    @NotNull(message = "coverImage flag is required") private Boolean coverImage;

    private Integer sortOrder;
}
