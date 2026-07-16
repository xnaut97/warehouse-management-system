package com.github.xnaut97.wms.service.user;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.user.ResetPasswordRequest;
import com.github.xnaut97.wms.dto.user.UpdateUserRequest;
import com.github.xnaut97.wms.dto.user.UserRequest;
import com.github.xnaut97.wms.dto.user.UserResponse;
import com.github.xnaut97.wms.entity.user.Role;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.user.RoleRepository;
import com.github.xnaut97.wms.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException("User not found"));

        return mapToResponse(user);

    }

    @Audit(
            action = AuditAction.CREATE,
            entity = "User"
    )
    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already exists");
        }

        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                userRepository.existsByEmail(request.getEmail())) {

            throw new BusinessException("Email already exists");
        }

        Role role = roleRepository.findByRole(
                RoleType.valueOf(request.getRole().toUpperCase())
        ).orElseThrow(() ->
                new BusinessException("Role not found"));

        User user = new User();

        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        user.setRole(role);

        userRepository.save(user);

        return mapToResponse(user);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "User"
    )
    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = findUserById(id);

        if (request.getEmail() != null &&
                !request.getEmail().equalsIgnoreCase(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {

            throw new BusinessException("Email already exists");
        }

        Role role = roleRepository.findByRole(
                RoleType.valueOf(request.getRole().toUpperCase())
        ).orElseThrow(() ->
                new BusinessException("Role not found"));

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(role);

        userRepository.save(user);

        return mapToResponse(user);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "User"
    )
    public UserResponse lockUser(Long id) {

        User user = findUserById(id);

        if (!user.getEnabled()) {
            throw new BusinessException("User is already locked");
        }

        user.setEnabled(false);

        userRepository.save(user);

        return mapToResponse(user);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "User"
    )
    public UserResponse unlockUser(Long id) {

        User user = findUserById(id);

        if (user.getEnabled()) {
            throw new BusinessException("User is already unlocked");
        }

        user.setEnabled(true);

        userRepository.save(user);

        return mapToResponse(user);

    }

    @Audit(
            action = AuditAction.UPDATE,
            entity = "User"
    )
    public void resetPassword(Long id, ResetPasswordRequest request) {

        User user = findUserById(id);

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

    }

    public void delete(Long id) {
        User user = findUserById(id);
        if(user == null) return;

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .role(user.getRole().getRole().name())
                .build();

    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    public User findByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException("User not found"));

    }

}