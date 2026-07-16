package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardInventoryTrendResponse {

    private String month;

    private BigDecimal stockIn;

    private BigDecimal stockOut;

    private BigDecimal stockBalance;

}
