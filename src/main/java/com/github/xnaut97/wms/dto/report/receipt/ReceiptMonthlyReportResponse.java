package com.github.xnaut97.wms.dto.report.receipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptMonthlyReportResponse {

    private Integer month;

    private Long totalReceipts;

    private BigDecimal totalQuantity;

    private BigDecimal totalAmount;

}