package com.github.xnaut97.wms.dto.alert;

import com.github.xnaut97.wms.enums.AlertRiskGroup;
import com.github.xnaut97.wms.enums.AlertSeverity;
import com.github.xnaut97.wms.enums.AlertType;
import com.github.xnaut97.wms.enums.StockGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AlertItemResponse {

    private AlertSeverity severity;

    private AlertType type;

    private AlertRiskGroup riskGroup;

    private StockGroup group;

    private String itemCode;

    private String itemName;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String lotNumber;

    private LocalDate expirationDate;

    private Long daysToExpiry;

    private String detail;

    private boolean lotTracked;

}
