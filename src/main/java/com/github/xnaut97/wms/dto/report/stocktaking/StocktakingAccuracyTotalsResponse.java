package com.github.xnaut97.wms.dto.report.stocktaking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StocktakingAccuracyTotalsResponse {

    private BigDecimal totalSystemQuantity;

    private BigDecimal totalPhysicalQuantity;

    private BigDecimal netVarianceQuantity;

    private BigDecimal absoluteVarianceQuantity;

    private BigDecimal netVarianceValue;

    private BigDecimal absoluteVarianceValue;

    private Long totalItems;

    private Long discrepancyItems;

}
