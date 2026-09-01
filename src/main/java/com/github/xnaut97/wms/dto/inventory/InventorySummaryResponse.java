package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.StockGroup;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class InventorySummaryResponse {

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

    private BigDecimal totalInventoryValue;

    private List<InventorySummaryRowResponse> items;

}
