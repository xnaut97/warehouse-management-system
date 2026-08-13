package com.github.xnaut97.wms.dto.bom;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BOMResponse {

    private Long id;

    private String code;

    private Long productId;

    private String productCode;

    private String productName;

    private Boolean enabled;

    private List<BOMItemResponse> items;
}