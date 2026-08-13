package com.github.xnaut97.wms.dto.product.issue;

import com.github.xnaut97.wms.enums.IssueStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProductIssueDetailResponse {

    private Long id;

    private String issueNo;

    private String warehouse;

    private String customer;

    private LocalDate issueDate;

    private IssueStatus status;

    private BigDecimal totalAmount;

    private List<ProductIssueItemResponse> items;
}