package com.github.xnaut97.wms.dto.report.inventory;

import com.github.xnaut97.wms.enums.InventoryTransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryResponse {

    private LocalDateTime transactionDate;

    private String referenceNo;

    private InventoryTransactionType transactionType;

    private BigDecimal quantity;

    private String warehouse;

    private String materialCode;

    private String materialName;

}