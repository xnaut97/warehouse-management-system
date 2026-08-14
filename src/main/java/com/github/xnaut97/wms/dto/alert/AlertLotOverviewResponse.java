package com.github.xnaut97.wms.dto.alert;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlertLotOverviewResponse {

    private AlertLotKpiResponse kpi;

    private List<ExpiryBucketResponse> expiryDistribution;

    private List<NearExpiryLotResponse> topNearExpiryLots;

}
