package com.github.xnaut97.wms.dto.supplier;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplierResponse {

    private Long id;

    private String code;

    private String name;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    private Boolean enabled;

}