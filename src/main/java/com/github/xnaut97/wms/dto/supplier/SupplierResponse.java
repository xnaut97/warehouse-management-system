package com.github.xnaut97.wms.dto.supplier;

import com.github.xnaut97.wms.enums.SupplierGroup;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplierResponse {

    private Long id;

    private String code;

    private String name;

    private SupplierGroup supplierGroup;

    private String contactPerson;

    private String phone;

    private String email;

    private String address;

    private String note;

    private Boolean enabled;

}
