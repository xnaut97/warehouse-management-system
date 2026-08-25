package com.github.xnaut97.wms.dto.bom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateBOMRequest {

    @NotBlank(message = "Mã BOM không được để trống")
    private String code;

    @NotEmpty(message = "BOM phải có ít nhất một nguyên vật liệu")
    @Valid
    private List<BOMItemRequest> items;
}