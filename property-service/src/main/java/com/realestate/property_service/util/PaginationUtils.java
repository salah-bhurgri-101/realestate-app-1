package com.realestate.property_service.util;

import com.realestate.property_service.dto.response.PageResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;

public class PaginationUtils {

    private PaginationUtils() {}

    public static <R> PageResponseDto<R> toPageResponse(Page<?> page, List<R> mappedContent) {
        return PageResponseDto.<R>builder()
                .content(mappedContent)
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
