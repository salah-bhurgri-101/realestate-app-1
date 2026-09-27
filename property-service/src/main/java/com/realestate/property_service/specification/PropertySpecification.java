package com.realestate.property_service.specification;

import com.realestate.property_service.dto.response.PropertySearchFilterDto;
import com.realestate.property_service.entity.Property;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PropertySpecification {

    private PropertySpecification() {}

    public static Specification<Property> build(PropertySearchFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.add(
                        cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase()));
            }

            if (filter.getPropertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), filter.getPropertyType()));
            }

            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.getMinPrice()));
            }

            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.getMaxPrice()));
            }

            if (filter.getMinBedrooms() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bedrooms"), filter.getMinBedrooms()));
            }

            if (filter.getMaxBedrooms() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bedrooms"), filter.getMaxBedrooms()));
            }

            if (filter.getMinArea() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("area"), filter.getMinArea()));
            }

            if (filter.getMaxArea() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("area"), filter.getMaxArea()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getFeatured() != null) {
                predicates.add(cb.equal(root.get("featured"), filter.getFeatured()));
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }

            // public search should never leak inactive properties unless caller explicitly
            // sets status (e.g. an admin/seller dashboard query)
            predicates.add(cb.equal(root.get("active"), true));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
