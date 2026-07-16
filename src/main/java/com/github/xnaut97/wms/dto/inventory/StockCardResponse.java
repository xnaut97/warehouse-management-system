package com.github.xnaut97.wms.dto.inventory;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class StockCardResponse {

    private String warehouse;

    private String materialCode;

    private String materialName;

    private List<StockCardItemResponse> transactions;

    private BigDecimal openingBalance;

    private BigDecimal closingBalance;

}