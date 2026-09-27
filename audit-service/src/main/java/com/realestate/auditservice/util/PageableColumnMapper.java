// package com.realestate.auditservice.util;
//
// import java.util.Map;
// import java.util.stream.Collectors;
// import java.util.stream.StreamSupport;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
//
// public final class PageableColumnMapper {
//
//    private static final Map<String, String> PROPERTY_TO_COLUMN = Map.of(
//            "createdAt", "created_at",
//            "serviceName", "service_name",
//            "entityType", "entity_type",
//            "entityId", "entity_id",
//            "username", "username",
//            "action", "action",
//            "id", "id");
//
//    private PageableColumnMapper() {}
//
//    public static Pageable toColumnSort(Pageable pageable) {
//        if (pageable.getSort().isUnsorted()) {
//            return pageable;
//        }
//
//        Sort translated = Sort.by(StreamSupport.stream(pageable.getSort().spliterator(), false)
//                .map(order -> {
//                    String column = PROPERTY_TO_COLUMN.get(order.getProperty());
//                    if (column == null) {
//                        throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
//                    }
//                    return order.isAscending() ? Sort.Order.asc(column) : Sort.Order.desc(column);
//                })
//                .collect(Collectors.toList()));
//
//        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), translated);
//    }
// }
