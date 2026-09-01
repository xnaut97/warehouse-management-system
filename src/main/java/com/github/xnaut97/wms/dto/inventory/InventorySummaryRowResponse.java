package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.ExpiryStatus;
import com.github.xnaut97.wms.enums.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class InventorySummaryRowResponse {

    private Long itemId;

    private String code;

    private String name;

    private String unit;

    private BigDecimal openingQuantity;

    private BigDecimal receiptQuantity;

    private BigDecimal issueQuantity;

    private BigDecimal closingQuantity;

    private BigDecimal averagePrice;

    private BigDecimal inventoryValue;

    private BigDecimal minimumStock;

    private BigDecimal maximumStock;

    private InventoryStatus thresholdStatus;

    private ExpiryStatus expiryStatus;

    private List<InventoryLotResponse> lots;

}
