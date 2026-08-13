package com.github.xnaut97.wms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OperationAlertResponse {

    private List<BelowMinAlertResponse> belowMin;

    private List<AboveMaxAlertResponse> aboveMax;

    private List<NearExpirationAlertResponse> nearExpiration;

}
