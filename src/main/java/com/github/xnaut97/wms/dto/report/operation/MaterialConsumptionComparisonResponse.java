package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConsumptionComparisonResponse {

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String unit;

    private BigDecimal actualQuantity;

    private BigDecimal standardQuantity;

    private BigDecimal varianceQuantity;

    private BigDecimal wasteRatePercent;

}
