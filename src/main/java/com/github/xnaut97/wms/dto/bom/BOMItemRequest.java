package com.github.xnaut97.wms.dto.bom;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BOMItemRequest {

    @NotNull(message = "Nguyên vật liệu không được để trống")
    private Long materialId;

    @NotNull(message = "Định mức tiêu hao không được để trống")
    @DecimalMin(
            value = "0.0001",
            message = "Định mức tiêu hao phải lớn hơn 0"
    )
    private BigDecimal consumptionQuantity;

    @NotNull(message = "Tỷ lệ phối trộn không được để trống")
    @DecimalMin(
            value = "0",
            message = "Tỷ lệ phối trộn không được nhỏ hơn 0"
    )
    @DecimalMax(
            value = "100",
            message = "Tỷ lệ phối trộn không được lớn hơn 100"
    )
    private BigDecimal mixingRatio;

    @NotNull(message = "Tỷ lệ hao hụt tối đa không được để trống")
    @DecimalMin(
            value = "0",
            message = "Tỷ lệ hao hụt không được nhỏ hơn 0"
    )
    @DecimalMax(
            value = "100",
            message = "Tỷ lệ hao hụt không được lớn hơn 100"
    )
    private BigDecimal maxWasteRatio;
}