package com.github.xnaut97.wms.dto.stocktaking;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StocktakingItemResponse {

    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal systemQuantity;

    private BigDecimal physicalQuantity;

    private BigDecimal varianceQuantity;

}