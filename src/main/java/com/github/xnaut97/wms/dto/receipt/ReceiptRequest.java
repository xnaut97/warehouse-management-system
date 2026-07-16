package com.github.xnaut97.wms.dto.receipt;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReceiptRequest {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate receiptDate;

}