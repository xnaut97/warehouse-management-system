package com.github.xnaut97.wms.controller.user;

import com.github.xnaut97.wms.dto.auth.LoginRequest;
import com.github.xnaut97.wms.dto.auth.LoginResponse;
import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        return ApiResponse.success(
                "Login successful",
                response
        );

    }

}