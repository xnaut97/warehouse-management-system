package com.github.xnaut97.wms.dto.product.issue;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AddProductIssueItemRequest {

    @NotNull
    private Long productId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal unitPrice;
}