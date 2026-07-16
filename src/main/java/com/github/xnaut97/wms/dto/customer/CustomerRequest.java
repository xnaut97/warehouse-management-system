package com.github.xnaut97.wms.dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @Email
    private String email;

    @NotBlank
    private String address;

}