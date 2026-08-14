package com.github.xnaut97.wms.dto.report.stocktaking;

import com.github.xnaut97.wms.enums.StockGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StocktakingWarehouseVarianceResponse {

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private StockGroup group;

    private Long itemCount;

    private BigDecimal netVarianceQuantity;

    private BigDecimal absoluteVarianceQuantity;

    private BigDecimal netVarianceValue;

    private BigDecimal absoluteVarianceValue;

    private BigDecimal sharePercent;

}
