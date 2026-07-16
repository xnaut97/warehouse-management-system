package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardOverviewResponse {

    private long totalRawMaterials;

    private long totalFinishedProducts;

    private BigDecimal totalInventoryValue;

    private BigDecimal totalGoodsReceived;

    private BigDecimal totalGoodsIssued;

    private BigDecimal currentInventoryQuantity;

}
