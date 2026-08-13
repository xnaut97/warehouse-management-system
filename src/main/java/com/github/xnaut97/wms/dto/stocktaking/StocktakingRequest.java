package com.github.xnaut97.wms.dto.stocktaking;

import com.github.xnaut97.wms.enums.StocktakingType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StocktakingRequest {

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate stocktakingDate;

    private StocktakingType type;

    private Long stocktakerId;

    private String note;

}
