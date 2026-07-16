package com.github.xnaut97.wms.dto.issue;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateIssueRequest {

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long customerId;

    @NotNull
    private LocalDate issueDate;

}