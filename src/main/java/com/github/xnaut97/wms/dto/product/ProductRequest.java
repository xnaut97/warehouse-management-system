package com.github.xnaut97.wms.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String specification;

    @NotBlank
    private String unit;

    @NotNull
    @PositiveOrZero
    private BigDecimal sellingPrice;

    @PositiveOrZero
    private BigDecimal averagePrice;

    @NotBlank
    private String category;

    @NotNull
    @PositiveOrZero
    private BigDecimal minimumStock;

    @NotNull
    @PositiveOrZero
    private BigDecimal maximumStock;

}
