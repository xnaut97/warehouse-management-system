package com.github.xnaut97.wms.dto.stocktaking;

import com.github.xnaut97.wms.enums.StocktakingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StocktakingResponse {

    private Long id;

    private String stocktakingNo;

    private String warehouse;

    private LocalDate stocktakingDate;

    private StocktakingStatus status;

    private String note;

}