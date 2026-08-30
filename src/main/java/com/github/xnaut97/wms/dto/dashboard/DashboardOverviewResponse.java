package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardOverviewResponse {

    private long totalMaterials;

    private long totalProducts;

    private BigDecimal totalInventoryValue;

    private long totalGoodsReceived;

    private long totalGoodsIssued;

    private BigDecimal currentInventoryQuantity;

}
