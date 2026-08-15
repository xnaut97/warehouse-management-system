package com.github.xnaut97.wms.dto.stocktaking;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveStocktakingCountRequest {

    @Valid
    private List<StocktakingCountLineRequest> items;

    @Valid
    private List<StocktakingCountLineRequest> batches;

}
