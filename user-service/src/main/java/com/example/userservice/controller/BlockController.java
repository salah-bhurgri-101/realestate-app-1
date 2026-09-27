package com.example.userservice.controller;

import com.example.userservice.service.BlockService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<Void> block(@PathVariable UUID userId, @PathVariable UUID targetUserId) {
        blockService.block(userId, targetUserId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unblock(@PathVariable UUID userId, @PathVariable UUID targetUserId) {
        blockService.unblock(userId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{targetUserId}/status")
    public ResponseEntity<Boolean> isBlocked(@PathVariable UUID userId, @PathVariable UUID targetUserId) {
        return ResponseEntity.ok(blockService.isBlocked(userId, targetUserId));
    }

    @GetMapping
    public ResponseEntity<Page<UUID>> listBlocked(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(blockService.listBlocked(userId, pageable));
    }
}
