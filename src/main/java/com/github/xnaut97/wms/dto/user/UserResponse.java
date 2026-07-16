package com.github.xnaut97.wms.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private String role;

    private Boolean enabled;

}