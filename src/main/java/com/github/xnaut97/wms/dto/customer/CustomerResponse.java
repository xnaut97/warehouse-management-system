package com.github.xnaut97.wms.dto.customer;

import com.github.xnaut97.wms.enums.CustomerGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerResponse {

    private Long id;

    private String code;

    private String name;

    private String address;

    private CustomerGroup customerGroup;

    private String receiverName;

    private String phone;

    private String email;

    private String note;

    private Boolean enabled;

}
