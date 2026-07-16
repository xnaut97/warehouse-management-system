package com.github.xnaut97.wms.dto.report.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportResponse {

    private Long warehouseId;

    private String warehouse;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal minimumStock;

    private BigDecimal inventoryValue;

    private String status;

}