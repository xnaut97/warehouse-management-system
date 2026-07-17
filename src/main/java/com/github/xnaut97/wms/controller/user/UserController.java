package com.github.xnaut97.wms.controller.user;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.user.ResetPasswordRequest;
import com.github.xnaut97.wms.dto.user.UpdateUserRequest;
import com.github.xnaut97.wms.dto.user.UserRequest;
import com.github.xnaut97.wms.dto.user.UserResponse;
import com.github.xnaut97.wms.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserResponse>> getAllUsers(
            Pageable pageable
    ) {

        return ApiResponse.success(
                "Users retrieved successfully",
                userService.getAllUsers(pageable)
        );

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "User retrieved successfully",
                userService.getUserById(id)
        );

    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> lockUser(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "User locked successfully",
                userService.lockUser(id)
        );

    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> unlockUser(
            @PathVariable Long id
    ) {

        return ApiResponse.success(
                "User unlocked successfully",
                userService.unlockUser(id)
        );

    }

    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long id,
            @RequestBody @Valid ResetPasswordRequest request
    ) {

        userService.resetPassword(id, request);

        return ApiResponse.success("Password reset successfully");

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request
    ) {

        return ApiResponse.success(
                "User updated successfully",
                userService.updateUser(id, request)
        );

    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createUser(
            @RequestBody @Valid UserRequest request
    ) {
        return ApiResponse.success(
                "User created successfully",
                userService.createUser(request)
        );
    }

}
