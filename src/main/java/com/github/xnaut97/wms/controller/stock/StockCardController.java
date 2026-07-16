package com.github.xnaut97.wms.controller.stock;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.inventory.StockCardResponse;
import com.github.xnaut97.wms.service.stock.StockCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stock-cards")
@RequiredArgsConstructor
public class StockCardController {

    private final StockCardService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'WAREHOUSE_STAFF',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<StockCardResponse> getStockCard(

            @RequestParam
            Long warehouseId,

            @RequestParam
            Long materialId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate

    ) {

        return ApiResponse.success(

                "Stock card retrieved successfully",

                service.getStockCard(

                        warehouseId,

                        materialId,

                        fromDate,

                        toDate

                )

        );

    }
}
