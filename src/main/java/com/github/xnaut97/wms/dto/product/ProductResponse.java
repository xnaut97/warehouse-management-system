package com.github.xnaut97.wms.dto.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ProductResponse {

    private Long id;

    private String code;

    private String name;

    private String specification;

    private String unit;

    private BigDecimal averagePrice;

    private String category;

    private BigDecimal minimumStock;

    private BigDecimal maximumStock;

    private Boolean enabled;

}
