package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.StockStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LowStockResponse {

    private Long inventoryId;

    private Long warehouseId;

    private String warehouse;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal currentStock;

    private BigDecimal minimumStock;

    private String unit;

    private StockStatus status;

}