package com.github.xnaut97.wms.dto.product.receipt;

import com.github.xnaut97.wms.enums.ReceiptStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ProductReceiptResponse {

    private Long id;

    private String receiptNo;

    private String supplier;

    private String warehouse;

    private LocalDate receiptDate;

    private ReceiptStatus status;

    private BigDecimal totalAmount;
}