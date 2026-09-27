package com.example.userservice.service;

import com.example.userservice.domain.UserBlock;
import com.example.userservice.exception.InvalidOperationException;
import com.example.userservice.repository.UserBlockRepository;
import com.example.userservice.repository.UserFollowRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final UserBlockRepository blockRepository;
    private final UserFollowRepository followRepository;

    @Transactional
    public void block(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new InvalidOperationException("A user cannot block themselves.");
        }
        if (!blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            blockRepository.save(UserBlock.builder()
                    .blockerId(blockerId)
                    .blockedId(blockedId)
                    .build());
        }
        // Blocking implies severing any existing follow relationship either direction.
        followRepository.findByFollowerIdAndFolloweeId(blockerId, blockedId).ifPresent(followRepository::delete);
        followRepository.findByFollowerIdAndFolloweeId(blockedId, blockerId).ifPresent(followRepository::delete);
    }

    @Transactional
    public void unblock(UUID blockerId, UUID blockedId) {
        blockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId).ifPresent(blockRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(UUID blockerId, UUID blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Transactional(readOnly = true)
    public Page<UUID> listBlocked(UUID blockerId, Pageable pageable) {
        return blockRepository.findByBlockerId(blockerId, pageable).map(UserBlock::getBlockedId);
    }
}
