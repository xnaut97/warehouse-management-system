package com.github.xnaut97.wms.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StockCardItemResponse {

    private LocalDateTime transactionDate;

    private String referenceNo;

    private String transactionType;

    private BigDecimal quantityIn;

    private BigDecimal quantityOut;

    private BigDecimal balance;

}