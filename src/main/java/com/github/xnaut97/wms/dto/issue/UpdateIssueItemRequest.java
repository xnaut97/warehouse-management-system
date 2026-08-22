package com.github.xnaut97.wms.dto.issue;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateIssueItemRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    private BigDecimal unitPrice;

}