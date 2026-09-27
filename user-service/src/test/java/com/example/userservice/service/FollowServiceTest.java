package com.example.userservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.userservice.domain.UserFollow;
import com.example.userservice.dto.response.FollowResponse;
import com.example.userservice.exception.InvalidOperationException;
import com.example.userservice.repository.UserFollowRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserFollowRepository followRepository;

    @InjectMocks
    private FollowService followService;

    @Test
    void follow_shouldRejectSelfFollow() {
        UUID userId = UUID.randomUUID();
        assertThatThrownBy(() -> followService.follow(userId, userId)).isInstanceOf(InvalidOperationException.class);
        verifyNoInteractions(followRepository);
    }

    @Test
    void follow_shouldCreateNewFollow_whenNotAlreadyFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(false);
        when(followRepository.save(any(UserFollow.class))).thenAnswer(inv -> {
            UserFollow f = inv.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });

        FollowResponse response = followService.follow(followerId, followeeId);

        assertThat(response.getFollowerId()).isEqualTo(followerId);
        assertThat(response.getFolloweeId()).isEqualTo(followeeId);
        verify(followRepository, times(1)).save(any(UserFollow.class));
    }

    @Test
    void follow_shouldReturnExisting_whenAlreadyFollowing() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        UserFollow existing = UserFollow.builder()
                .id(UUID.randomUUID())
                .followerId(followerId)
                .followeeId(followeeId)
                .build();

        when(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(true);
        when(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
                .thenReturn(Optional.of(existing));

        FollowResponse response = followService.follow(followerId, followeeId);

        assertThat(response.getId()).isEqualTo(existing.getId());
        verify(followRepository, never()).save(any());
    }
}
