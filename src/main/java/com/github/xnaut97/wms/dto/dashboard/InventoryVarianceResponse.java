package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class InventoryVarianceResponse {

    private long totalStocktakingRecords;

    private BigDecimal totalVarianceQuantity;

    private BigDecimal totalVarianceValue;

    private List<TopVarianceItemResponse> topVarianceItems;

}
