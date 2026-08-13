package com.github.xnaut97.wms.dto.stocktaking;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class StocktakingItemBatchResponse {

    private Long id;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal systemQuantity;

    private BigDecimal physicalQuantity;

    private BigDecimal varianceQuantity;

    private String reason;

}
