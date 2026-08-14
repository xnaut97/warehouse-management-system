package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MaterialQuantityResponse {

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String unit;

    private BigDecimal unitPrice;

    private BigDecimal quantity;

}
