package com.github.xnaut97.wms.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LowStockAlertResponse {

    private Long materialId;

    private String materialCode;

    private String materialName;

    private String warehouse;

    private BigDecimal currentQuantity;

    private BigDecimal minimumStock;

}