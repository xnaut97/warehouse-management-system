package com.github.xnaut97.wms.dto.receipt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateReceiptItemRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal unitPrice;

}