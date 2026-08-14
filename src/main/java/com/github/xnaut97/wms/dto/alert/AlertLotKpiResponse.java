package com.github.xnaut97.wms.dto.alert;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AlertLotKpiResponse {

    private long totalLotsInStock;

    private long redAlertLots;

    private long expiredLots;

    private BigDecimal expiredLotValue;

    private long lotsWithoutExpiry;

}
