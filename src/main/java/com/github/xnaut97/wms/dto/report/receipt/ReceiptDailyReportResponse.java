package com.github.xnaut97.wms.dto.report.receipt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDailyReportResponse {

    private LocalDate date;

    private Long totalReceipts;

    private BigDecimal totalQuantity;

    private BigDecimal totalAmount;

}