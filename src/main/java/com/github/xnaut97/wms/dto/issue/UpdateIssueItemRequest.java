package com.github.xnaut97.wms.dto.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateIssueItemRequest {

    @NotNull
    private BigDecimal quantity;

    @NotNull
    private BigDecimal unitPrice;

}