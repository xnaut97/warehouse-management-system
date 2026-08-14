package com.github.xnaut97.wms.dto.alert;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AlertCenterResponse {

    private AlertSummaryResponse summary;

    private List<AlertItemResponse> alerts;

}
