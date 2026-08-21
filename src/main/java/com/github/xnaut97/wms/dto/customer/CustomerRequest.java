package com.github.xnaut97.wms.dto.customer;

import com.github.xnaut97.wms.enums.CustomerGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest {

    @NotBlank(message = "Mã khách hàng không được để trống")
    @Size(max = 30)
    private String code;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String address;

    @NotNull(message = "Nhóm khách hàng không được để trống")
    private CustomerGroup customerGroup;

    @Size(max = 100)
    private String receiverName;

    @Size(max = 20)
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 1000)
    private String note;

}
