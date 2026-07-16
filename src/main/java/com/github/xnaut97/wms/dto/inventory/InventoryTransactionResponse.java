package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.InventoryTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InventoryTransactionResponse {

    private Long id;

    private String warehouse;

    private String materialCode;

    private String materialName;

    private InventoryTransactionType type;

    private BigDecimal quantity;

    private String referenceNo;

    private String createdBy;

    private LocalDateTime createdAt;

}