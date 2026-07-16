package com.github.xnaut97.wms.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class InventoryResponse {

    private Long id;

    private Long warehouseId;

    private String warehouse;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal quantity;

}