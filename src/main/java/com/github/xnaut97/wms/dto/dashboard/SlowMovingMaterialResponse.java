package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SlowMovingMaterialResponse {

    private String code;

    private String name;

    private BigDecimal currentQuantity;

    private LocalDateTime lastIssueDate;

}
