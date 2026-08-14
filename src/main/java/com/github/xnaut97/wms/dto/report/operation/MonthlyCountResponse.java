package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MonthlyCountResponse {

    private Integer year;

    private Integer month;

    private Long total;

}
