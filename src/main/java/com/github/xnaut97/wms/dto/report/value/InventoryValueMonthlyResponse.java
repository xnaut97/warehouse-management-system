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
public class InventoryValueMonthlyResponse {

    private String month;

    private BigDecimal materialValue;

    private BigDecimal productValue;

    private BigDecimal totalValue;

}
