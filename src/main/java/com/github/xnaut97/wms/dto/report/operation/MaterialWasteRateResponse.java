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
public class MaterialWasteRateResponse {

    private boolean available;

    private String unavailableReason;

    private BigDecimal wasteRatePercent;

    private BigDecimal wasteRateByValuePercent;

    private BigDecimal totalActualQuantity;

    private BigDecimal totalStandardQuantity;

    private BigDecimal totalActualValue;

    private BigDecimal totalStandardValue;

    private Integer comparedMaterials;

}
