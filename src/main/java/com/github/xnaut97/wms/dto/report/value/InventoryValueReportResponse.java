package com.github.xnaut97.wms.dto.report.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryValueReportResponse {

    private LocalDate fromDate;

    private LocalDate toDate;

    private BigDecimal totalInventoryValue;

    private BigDecimal materialValue;

    private BigDecimal productValue;

    private BigDecimal materialPercentage;

    private BigDecimal productPercentage;

    private List<InventoryValueGroupResponse> groups;

    private List<InventoryValueMonthlyResponse> monthlyTrend;

    private InventoryTurnoverResponse turnover;

}
