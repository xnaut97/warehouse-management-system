package com.github.xnaut97.wms.dto.report.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTurnoverResponse {

    private boolean available;

    private String unavailableReason;

    private BigDecimal ratio;

    private BigDecimal costOfGoodsIssued;

    private BigDecimal averageInventoryValue;

    private Integer periodMonths;

}
