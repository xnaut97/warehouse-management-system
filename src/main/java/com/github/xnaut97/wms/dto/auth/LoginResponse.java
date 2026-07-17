package com.github.xnaut97.wms.dto.auth;

import com.github.xnaut97.wms.dto.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private UserResponse user;

}