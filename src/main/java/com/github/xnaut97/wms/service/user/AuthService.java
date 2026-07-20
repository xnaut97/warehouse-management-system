package com.github.xnaut97.wms.service.user;

import com.github.xnaut97.wms.dto.auth.LoginRequest;
import com.github.xnaut97.wms.dto.auth.LoginResponse;
import com.github.xnaut97.wms.dto.user.UserResponse;
import com.github.xnaut97.wms.entity.user.User;
import com.github.xnaut97.wms.enums.RoleType;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.security.CustomUserDetails;
import com.github.xnaut97.wms.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        if(details == null)
            throw new BusinessException("Không tìm thấy người dùng");

        User user = details.getUser();

        String token = jwtService.generateToken(details.getUsername());

        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getRole().name())
                .enabled(user.getEnabled())
                .build();

        return new LoginResponse(token, response);

    }

}