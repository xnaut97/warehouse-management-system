package com.github.xnaut97.wms.enums;

public enum AlertType {

    BELOW_MIN(AlertRiskGroup.SUPPLY_THRESHOLD),

    ABOVE_MAX(AlertRiskGroup.SUPPLY_THRESHOLD),

    NEAR_EXPIRY(AlertRiskGroup.QUALITY_RISK),

    STOCKTAKING_VARIANCE(AlertRiskGroup.INTERNAL_CONTROL);

    private final AlertRiskGroup riskGroup;

    AlertType(AlertRiskGroup riskGroup) {
        this.riskGroup = riskGroup;
    }

    public AlertRiskGroup getRiskGroup() {
        return riskGroup;
    }

}
