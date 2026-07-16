package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DashboardMonthlyQuantityResponse {

    private Integer year;

    private Integer month;

    private BigDecimal quantity;

}
