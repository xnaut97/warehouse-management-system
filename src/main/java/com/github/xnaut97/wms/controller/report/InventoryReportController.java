package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.inventory.InventoryHistoryResponse;
import com.github.xnaut97.wms.dto.report.inventory.InventoryReportResponse;
import com.github.xnaut97.wms.service.report.InventoryReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/inventory")
@RequiredArgsConstructor
public class InventoryReportController {

    private final InventoryReportService service;

    @GetMapping("/raw-materials")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<Page<InventoryReportResponse>> rawMaterialInventoryReport(
            Pageable pageable
    ) {

        return ApiResponse.success(

                "Inventory report retrieved successfully",

                service.getRawMaterialInventoryReport(pageable)

        );

    }

    @GetMapping("/history")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<Page<InventoryHistoryResponse>> inventoryHistoryReport(

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Pageable pageable

    ) {

        return ApiResponse.success(

                "Inventory history retrieved successfully",

                service.getInventoryHistoryReport(

                        warehouseId,

                        fromDate,

                        toDate,

                        pageable

                )

        );

    }

}
