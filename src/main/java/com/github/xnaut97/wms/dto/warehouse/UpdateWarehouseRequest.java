package com.github.xnaut97.wms.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWarehouseRequest {

    @NotBlank
    private String name;

    private String address;

    private String description;

    private Long managerId;

    private Boolean enabled;

}