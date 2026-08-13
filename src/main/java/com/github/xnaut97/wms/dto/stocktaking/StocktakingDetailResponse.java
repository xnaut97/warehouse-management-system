package com.github.xnaut97.wms.dto.stocktaking;

import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.enums.StocktakingType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class StocktakingDetailResponse {

    private Long id;

    private String stocktakingNo;

    private Long warehouseId;

    private String warehouse;

    private StockGroup warehouseGroup;

    private LocalDate stocktakingDate;

    private StocktakingType type;

    private StocktakingStatus status;

    private String stocktaker;

    private String note;

    private List<StocktakingItemResponse> items;

}
