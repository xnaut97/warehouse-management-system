package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InventoryAnalysisResponse {

    private BigDecimal materialInventory;

    private BigDecimal productInventory;

    private BigDecimal stockIn;

    private BigDecimal stockOut;

    private BigDecimal stockBalance;

    private BigDecimal inventoryValue;

    private BigDecimal materialInventoryValue;

    private BigDecimal productInventoryValue;

}
