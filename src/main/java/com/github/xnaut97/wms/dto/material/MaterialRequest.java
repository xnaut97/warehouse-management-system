package com.github.xnaut97.wms.dto.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MaterialRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String unit;

    @NotNull
    @Positive
    private BigDecimal unitPrice;

    @NotNull
    private BigDecimal minimumStock;

    @NotNull
    private Long supplierId;

}