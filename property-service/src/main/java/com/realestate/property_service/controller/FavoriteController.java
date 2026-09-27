package com.realestate.property_service.controller;

import com.realestate.property_service.dto.response.FavoriteResponseDto;
import com.realestate.property_service.dto.response.PageResponseDto;
import com.realestate.property_service.service.FavoriteService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/api/properties/{propertyId}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(
            @AuthenticationPrincipal UUID buyerId, @PathVariable UUID propertyId) {
        boolean added = favoriteService.toggleFavorite(buyerId, propertyId);
        return ResponseEntity.ok(Map.of("favorited", added));
    }

    @GetMapping("/api/favorites/my")
    public ResponseEntity<PageResponseDto<FavoriteResponseDto>> getMyFavorites(
            @AuthenticationPrincipal UUID buyerId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(buyerId, pageNo, pageSize));
    }
}
