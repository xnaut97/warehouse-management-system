package com.github.xnaut97.wms.dto.report.operation;

import com.github.xnaut97.wms.enums.StockGroup;
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
public class StockSummaryReportResponse {

    private LocalDate fromDate;

    private LocalDate toDate;

    private StockGroup stockGroup;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private BigDecimal totalOpeningQuantity;

    private BigDecimal totalReceiptQuantity;

    private BigDecimal totalIssueQuantity;

    private BigDecimal totalClosingQuantity;

    private List<StockSummaryRowResponse> items;

}
