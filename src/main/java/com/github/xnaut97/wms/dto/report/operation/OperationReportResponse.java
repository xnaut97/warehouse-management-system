package com.github.xnaut97.wms.dto.report.operation;

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
public class OperationReportResponse {

    private LocalDate fromDate;

    private LocalDate toDate;

    private MaterialWasteRateResponse wasteRate;

    private DocumentVolumeResponse documentVolume;

    private List<MaterialConsumptionComparisonResponse> materialComparisons;

    private List<WeekdayFrequencyResponse> weekdayFrequency;

}
