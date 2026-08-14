package com.github.xnaut97.wms.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AlertStockRow {

    private Long itemId;

    private String itemCode;

    private String itemName;

    private String unit;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private BigDecimal warehouseQuantity;

    private BigDecimal totalQuantity;

    private BigDecimal minimumStock;

    private BigDecimal maximumStock;

}
