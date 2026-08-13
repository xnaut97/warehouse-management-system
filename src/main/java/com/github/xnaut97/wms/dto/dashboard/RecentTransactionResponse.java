package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RecentTransactionResponse {

    private Long id;

    private String time;

    private String voucherNo;

    private String itemCode;

    private String transactionType;

    private String itemCategory;

    private BigDecimal quantity;

    private String status;

}
