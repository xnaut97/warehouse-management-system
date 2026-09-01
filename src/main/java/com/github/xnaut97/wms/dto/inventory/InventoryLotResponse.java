package com.github.xnaut97.wms.dto.inventory;

import com.github.xnaut97.wms.enums.ExpiryStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class InventoryLotResponse {

    private Long inventoryId;

    private String lotNumber;

    private LocalDate expirationDate;

    private Long daysToExpiry;

    private BigDecimal quantity;

    private ExpiryStatus status;

}
