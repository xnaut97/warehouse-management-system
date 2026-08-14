package com.github.xnaut97.wms.dto.report.stocktaking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StocktakingReasonRow {

    private String reason;

    private Long itemCount;

    private BigDecimal absoluteVarianceQuantity;

    private BigDecimal absoluteVarianceValue;

}
