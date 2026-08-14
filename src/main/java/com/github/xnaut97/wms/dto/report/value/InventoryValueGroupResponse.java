package com.github.xnaut97.wms.dto.report.value;

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
public class InventoryValueGroupResponse {

    private StockGroup group;

    private String warehouseCode;

    private BigDecimal value;

    private BigDecimal percentage;

}
