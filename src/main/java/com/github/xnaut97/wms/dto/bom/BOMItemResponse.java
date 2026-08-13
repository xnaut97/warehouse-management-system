package com.github.xnaut97.wms.dto.bom;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BOMItemResponse {

    private Long id;

    private Long materialId;

    private String materialCode;

    private String materialName;

    private BigDecimal consumptionQuantity;

    private String unit;

    private BigDecimal mixingRatio;

    private BigDecimal maxWasteRatio;
}