package com.github.xnaut97.wms.service.user;

import com.github.xnaut97.wms.dto.auth.LoginRequest;
import com.github.xnaut97.wms.dto.auth.LoginResponse;
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

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        if(user == null)
            throw new BusinessException("User not found");

        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(token);

    }

}