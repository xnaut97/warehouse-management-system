package com.github.xnaut97.wms.dto.receipt;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReceiptItemResponse {

    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

}