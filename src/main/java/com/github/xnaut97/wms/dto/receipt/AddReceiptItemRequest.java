package com.github.xnaut97.wms.dto.receipt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddReceiptItemRequest {

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal unitPrice;

}