package com.realestate.property_service.util;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    //    Use this where UserID need or required and @RequestHeader("X-User-Id") UUID sellerId/buyer/viewwe/admin etc,
    public UUID getCurrentUserId() {

        return UUID.fromString(
                SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
