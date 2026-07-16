package com.github.xnaut97.wms.dto.material;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MaterialResponse {

    private Long id;

    private String code;

    private String name;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal minimumStock;

    private Long supplierId;

    private String supplierName;

    private Boolean enabled;

}