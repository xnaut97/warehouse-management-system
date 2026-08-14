package com.github.xnaut97.wms.dto.report.stocktaking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StocktakingAccuracyReportResponse {

    private LocalDate fromDate;

    private LocalDate toDate;

    private StocktakingAccuracyResponse accuracy;

    private StocktakingVarianceValueResponse varianceValue;

    private List<StocktakingWarehouseVarianceResponse> warehouses;

    private List<StocktakingReasonResponse> reasons;

}
