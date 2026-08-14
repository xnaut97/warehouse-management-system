package com.github.xnaut97.wms.dto.report.stocktaking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class StocktakingWarehouseVarianceRow {

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Long itemCount;

    private BigDecimal netVarianceQuantity;

    private BigDecimal absoluteVarianceQuantity;

    private BigDecimal netVarianceValue;

    private BigDecimal absoluteVarianceValue;

    private Long productItemCount;

}
