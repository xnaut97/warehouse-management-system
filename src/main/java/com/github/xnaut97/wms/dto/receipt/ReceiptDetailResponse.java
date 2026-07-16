package com.github.xnaut97.wms.dto.receipt;

import com.github.xnaut97.wms.enums.ReceiptStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ReceiptDetailResponse {

    private Long id;

    private String receiptNo;

    private String supplier;

    private String warehouse;

    private LocalDate receiptDate;

    private ReceiptStatus status;

    private BigDecimal totalAmount;

    private List<ReceiptItemResponse> items;

}