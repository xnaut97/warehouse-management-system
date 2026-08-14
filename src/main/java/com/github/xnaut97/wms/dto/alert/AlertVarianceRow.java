package com.github.xnaut97.wms.dto.alert;

import com.github.xnaut97.wms.enums.StocktakingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AlertVarianceRow {

    private String itemCode;

    private String itemName;

    private String unit;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String stocktakingNo;

    private LocalDate stocktakingDate;

    private StocktakingStatus stocktakingStatus;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal systemQuantity;

    private BigDecimal physicalQuantity;

    private BigDecimal varianceQuantity;

    public AlertVarianceRow(
            String itemCode,
            String itemName,
            String unit,
            Long warehouseId,
            String warehouseCode,
            String warehouseName,
            String stocktakingNo,
            LocalDate stocktakingDate,
            StocktakingStatus stocktakingStatus,
            BigDecimal systemQuantity,
            BigDecimal physicalQuantity,
            BigDecimal varianceQuantity
    ) {
        this(
                itemCode,
                itemName,
                unit,
                warehouseId,
                warehouseCode,
                warehouseName,
                stocktakingNo,
                stocktakingDate,
                stocktakingStatus,
                null,
                null,
                systemQuantity,
                physicalQuantity,
                varianceQuantity
        );
    }

}
