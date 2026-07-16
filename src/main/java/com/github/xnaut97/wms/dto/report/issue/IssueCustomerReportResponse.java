package com.github.xnaut97.wms.dto.report.issue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCustomerReportResponse {

    private Long customerId;

    private String customerName;

    private Long totalIssues;

    private BigDecimal totalQuantity;

    private BigDecimal totalAmount;

}