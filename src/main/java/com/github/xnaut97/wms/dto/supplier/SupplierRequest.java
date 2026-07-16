package com.github.xnaut97.wms.dto.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    @NotBlank(message = "Supplier code is required")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 20)
    private String phone;

    @Email(message = "Invalid email")
    private String email;

    @Size(max = 255)
    private String address;

}