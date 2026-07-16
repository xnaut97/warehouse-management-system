package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopVarianceItemResponse {

    private String materialCode;

    private String materialName;

    private BigDecimal varianceQuantity;

    private BigDecimal varianceValue;

}
