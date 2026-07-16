package com.github.xnaut97.wms.dto.report.stocktaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StocktakingSummaryReportResponse {

    private Long totalStocktaking;

    private Long totalVarianceItems;

    private BigDecimal totalVarianceQuantity;

    private BigDecimal totalVarianceValue;

}