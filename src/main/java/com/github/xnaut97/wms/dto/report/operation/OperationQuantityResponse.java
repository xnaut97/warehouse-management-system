package com.github.xnaut97.wms.dto.report.operation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationQuantityResponse {

    private Long itemId;

    private String code;

    private String name;

    private String unit;

    private BigDecimal quantity;

}
