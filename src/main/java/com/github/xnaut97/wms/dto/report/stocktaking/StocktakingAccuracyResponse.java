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
public class StocktakingAccuracyResponse {

    private boolean available;

    private String unavailableReason;

    private BigDecimal accuracyPercent;

    private BigDecimal totalSystemQuantity;

    private BigDecimal totalPhysicalQuantity;

    private Long totalItems;

    private Long discrepancyItems;

    private Long completedStocktakings;

}
