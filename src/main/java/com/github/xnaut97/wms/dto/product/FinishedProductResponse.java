package com.github.xnaut97.wms.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinishedProductResponse {

    private Long id;

    private String code;

    private String name;

    private String specification;

    private String unit;

    private BigDecimal sellingPrice;

    private Boolean enabled;

}