package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryRowResponse {

    private Long itemId;

    private String code;

    private String name;

    private String unit;

    private BigDecimal openingQuantity;

    private BigDecimal receiptQuantity;

    private BigDecimal issueQuantity;

    private BigDecimal closingQuantity;

    private List<OperationDocumentResponse> documents;

}
