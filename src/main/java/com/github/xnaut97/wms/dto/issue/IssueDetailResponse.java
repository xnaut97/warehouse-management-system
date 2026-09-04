package com.github.xnaut97.wms.dto.issue;

import com.github.xnaut97.wms.enums.IssueStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class IssueDetailResponse {

    private Long id;

    private String issueNo;

    private Long warehouseId;

    private String warehouse;

    private String customer;

    private LocalDate issueDate;

    private IssueStatus status;

    private BigDecimal totalAmount;

    private String createdBy;

    private List<IssueItemResponse> items;

}