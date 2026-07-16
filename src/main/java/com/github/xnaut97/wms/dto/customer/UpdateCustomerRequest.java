package com.github.xnaut97.wms.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerRequest {

    @NotBlank
    private String name;

    private String phone;

    @Email
    private String email;

    private String address;

}