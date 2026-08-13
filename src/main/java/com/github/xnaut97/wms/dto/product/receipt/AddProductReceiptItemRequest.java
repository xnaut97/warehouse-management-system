package com.github.xnaut97.wms.dto.product.receipt;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AddProductReceiptItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    private String lotNumber;

    private java.time.LocalDate expirationDate;

    private BigDecimal unitPrice;
}