package com.realestate.auth_service.service;

import com.realestate.auth_service.dto.response.UserResponse;
import com.realestate.auth_service.entity.Role;
import com.realestate.auth_service.entity.User;
import com.realestate.auth_service.exception.UserNotFoundException;
import com.realestate.auth_service.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuthUserEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(Role role, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return (role == null ? userRepository.findAll(pageable) : userRepository.findAllByRole(role, pageable))
                .map(UserResponse::from)
                .getContent();
    }

    @Transactional
    public UserResponse setStatus(UUID userId, boolean active) {
        User user = find(userId);
        if (active) {
            userRepository.activate(userId);
        } else {
            userRepository.deactivate(userId);
            userRepository.incrementTokenVersion(userId);
        }
        user.setIsActive(active);
        eventPublisher.publish(user, active ? "USER_REACTIVATED" : "USER_DEACTIVATED");
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse setRole(UUID userId, Role role) {
        User user = find(userId);
        userRepository.updateRole(userId, role);
        user.setRole(role);
        eventPublisher.publish(user, "USER_UPDATED");
        return UserResponse.from(user);
    }

    private User find(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found."));
    }
}
