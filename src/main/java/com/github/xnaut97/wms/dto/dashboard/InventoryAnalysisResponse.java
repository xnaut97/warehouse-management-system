package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InventoryAnalysisResponse {

    private long rawMaterialInventory;

    private long finishedProductInventory;

    private BigDecimal stockIn;

    private BigDecimal stockOut;

    private BigDecimal stockBalance;

    private BigDecimal inventoryValue;

}
