package com.realestate.property_service.dto.response;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImageDto {

    private UUID id;

    private String imageUrl;

    private Boolean coverImage;

    private String fileName;

    private Long fileSize;

    private Integer sortOrder;
}
