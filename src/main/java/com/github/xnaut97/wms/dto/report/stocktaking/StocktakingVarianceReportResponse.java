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
public class StocktakingVarianceReportResponse {

    private String stocktakingNo;

    private String materialCode;

    private String materialName;

    private BigDecimal systemQuantity;

    private BigDecimal physicalQuantity;

    private BigDecimal varianceQuantity;

    private BigDecimal varianceValue;

}