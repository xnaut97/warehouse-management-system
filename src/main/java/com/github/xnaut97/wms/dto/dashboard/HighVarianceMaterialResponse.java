package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class HighVarianceMaterialResponse {

    private String code;

    private String name;

    private BigDecimal varianceQuantity;

    private BigDecimal varianceValue;

}
