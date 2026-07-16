package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DashboardSummaryResponse {

    private long warehouses;

    private long suppliers;

    private long customers;

    private long materials;

    private long inventoryRecords;

    private long lowStockItems;

    private long receiptsThisMonth;

    private long issuesThisMonth;

    private BigDecimal totalInventoryQuantity;

    private BigDecimal totalInventoryValue;

}