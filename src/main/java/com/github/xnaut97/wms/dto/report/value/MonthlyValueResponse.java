package com.github.xnaut97.wms.dto.report.value;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MonthlyValueResponse {

    private Integer year;

    private Integer month;

    private BigDecimal value;

}
