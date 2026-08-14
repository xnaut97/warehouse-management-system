package com.github.xnaut97.wms.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AlertSummaryResponse {

    private long total;

    private long critical;

    private long warning;

}
