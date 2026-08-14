package com.github.xnaut97.wms.dto.alert;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AlertLotRow {

    private Long itemId;

    private String itemCode;

    private String itemName;

    private String unit;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private String lotNumber;

    private LocalDate expirationDate;

    private BigDecimal quantity;

    private BigDecimal averagePrice;

}
