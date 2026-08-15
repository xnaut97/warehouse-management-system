package com.github.xnaut97.wms.dto.stocktaking;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StocktakingCountLineRequest {

    @NotNull
    private Long id;

    @DecimalMin("0.00")
    private BigDecimal physicalQuantity;

    private String reason;

}
