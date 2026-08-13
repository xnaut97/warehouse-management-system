package com.github.xnaut97.wms.dto.stocktaking;

import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class StocktakingItemResponse {

    private Long id;

    private StockGroup itemGroup;

    private Long materialId;

    private Long productId;

    private String code;

    private String name;

    private String unit;

    private BigDecimal systemQuantity;

    private BigDecimal physicalQuantity;

    private BigDecimal varianceQuantity;

    private StocktakingItemStatus itemStatus;

    private String reason;

    private boolean batchManaged;

    private List<StocktakingItemBatchResponse> batches;

}
