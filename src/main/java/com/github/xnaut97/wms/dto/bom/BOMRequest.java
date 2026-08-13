package com.github.xnaut97.wms.dto.bom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BOMRequest {

    @NotBlank(message = "Mã BOM không được để trống")
    private String code;

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    @NotEmpty(message = "BOM phải có ít nhất một nguyên vật liệu")
    @Valid
    private List<BOMItemRequest> items;
}