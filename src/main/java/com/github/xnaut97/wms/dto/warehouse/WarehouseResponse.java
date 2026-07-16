package com.github.xnaut97.wms.dto.warehouse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseResponse {

    private Long id;

    private String code;

    private String name;

    private String address;

    private String description;

    private Long managerId;

    private String managerName;

    private Boolean enabled;

}