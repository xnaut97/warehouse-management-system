package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.StockGroup;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InventoryResponse {

    private Long id;

    private StockGroup itemGroup;

    private Long warehouseId;

    private String warehouse;

    private Long materialId;

    private Long productId;

    private String code;

    private String name;

    private String unit;

    private BigDecimal quantity;

}
