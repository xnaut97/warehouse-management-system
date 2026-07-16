package com.github.xnaut97.wms.dto.report.stocktaking;

import com.github.xnaut97.wms.enums.StocktakingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StocktakingReportResponse {

    private String stocktakingNo;

    private String warehouse;

    private LocalDate stocktakingDate;

    private StocktakingStatus status;

    private String createdBy;

}