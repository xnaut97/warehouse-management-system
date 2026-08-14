package com.github.xnaut97.wms.dto.alert;

import com.github.xnaut97.wms.enums.StockGroup;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class NearExpiryLotResponse {

    private StockGroup group;

    private String itemCode;

    private String itemName;

    private String unit;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String lotNumber;

    private LocalDate expirationDate;

    private Long daysToExpiry;

    private BigDecimal lotQuantity;

    private BigDecimal averagePrice;

    private BigDecimal stockValue;

    private boolean lotTracked;

}
