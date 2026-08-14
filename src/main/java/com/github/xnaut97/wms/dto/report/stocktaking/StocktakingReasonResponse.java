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
public class StocktakingReasonResponse {

    private String reason;

    private boolean unspecified;

    private Long itemCount;

    private BigDecimal absoluteVarianceQuantity;

    private BigDecimal absoluteVarianceValue;

}
