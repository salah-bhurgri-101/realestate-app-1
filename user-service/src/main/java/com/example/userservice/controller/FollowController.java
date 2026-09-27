package com.example.userservice.controller;

import com.example.userservice.dto.response.FollowCountsResponse;
import com.example.userservice.dto.response.FollowResponse;
import com.example.userservice.service.FollowService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<FollowResponse> follow(@PathVariable UUID userId, @PathVariable UUID targetUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(followService.follow(userId, targetUserId));
    }

    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID userId, @PathVariable UUID targetUserId) {
        followService.unfollow(userId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/following")
    public ResponseEntity<Page<FollowResponse>> getFollowing(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowing(userId, pageable));
    }

    @GetMapping("/followers")
    public ResponseEntity<Page<FollowResponse>> getFollowers(@PathVariable UUID userId, Pageable pageable) {
        return ResponseEntity.ok(followService.getFollowers(userId, pageable));
    }

    @GetMapping("/counts")
    public ResponseEntity<FollowCountsResponse> getCounts(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getCounts(userId));
    }
}
