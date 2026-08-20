package com.github.xnaut97.wms.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ProductStockResponse {

    private Long inventoryId;

    private Long warehouseId;

    private Long productId;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal quantity;
}
