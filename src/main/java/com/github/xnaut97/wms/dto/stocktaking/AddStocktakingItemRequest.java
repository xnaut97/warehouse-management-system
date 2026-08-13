package com.github.xnaut97.wms.dto.stocktaking;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddStocktakingItemRequest {

    private Long materialId;

    private Long productId;

    @DecimalMin("0.00")
    private BigDecimal physicalQuantity;

    private String reason;

}
