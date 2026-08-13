package com.github.xnaut97.wms.dto.product.issue;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ProductIssueItemResponse {

    private Long id;

    private Long productId;

    private String productCode;

    private String productName;

    private String unit;

    private BigDecimal quantity;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal unitPrice;

    private BigDecimal amount;
}