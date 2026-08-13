package com.github.xnaut97.wms.dto.product.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProductIssueRequest {

    @NotNull
    private Long warehouseId;

    private Long customerId;

    @NotNull
    private LocalDate issueDate;
}