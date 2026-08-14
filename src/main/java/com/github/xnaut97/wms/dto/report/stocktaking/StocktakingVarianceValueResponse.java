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
public class StocktakingVarianceValueResponse {

    private BigDecimal netVarianceValue;

    private BigDecimal absoluteVarianceValue;

    private BigDecimal netVarianceQuantity;

    private BigDecimal absoluteVarianceQuantity;

}
