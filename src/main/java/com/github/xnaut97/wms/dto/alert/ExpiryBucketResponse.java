package com.github.xnaut97.wms.dto.alert;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExpiryBucketResponse {

    private String bucket;

    private String label;

    private long materialLots;

    private long productLots;

    private BigDecimal materialQuantity;

    private BigDecimal productQuantity;

    private BigDecimal materialValue;

    private BigDecimal productValue;

}
