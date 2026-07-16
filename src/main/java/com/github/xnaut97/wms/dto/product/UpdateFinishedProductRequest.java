package com.github.xnaut97.wms.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateFinishedProductRequest {

    @NotBlank
    private String name;

    private String specification;

    @NotBlank
    private String unit;

    @NotNull
    private BigDecimal sellingPrice;

    @NotNull
    private Boolean enabled;

}