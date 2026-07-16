package com.github.xnaut97.wms.dto.customer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CustomerResponse {

    private Long id;

    private String name;

    private String phone;

    private String email;

    private String address;

    private Boolean enabled;

}