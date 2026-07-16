package com.github.xnaut97.wms.dto.report.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueDailyReportResponse {

    private LocalDate date;

    private Long totalIssues;

    private BigDecimal totalQuantity;

    private BigDecimal totalAmount;

}