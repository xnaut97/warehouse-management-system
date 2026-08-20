package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.value.InventoryValueReportResponse;
import com.github.xnaut97.wms.service.report.InventoryValueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/inventory-value")
@RequiredArgsConstructor
public class InventoryValueReportController {

    private final InventoryValueReportService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD',
            'ACCOUNTANT'
            )
            """)
    public ApiResponse<InventoryValueReportResponse> report(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate

    ) {

        return ApiResponse.success(

                "Inventory value report retrieved successfully",

                service.getReport(fromDate, toDate)

        );

    }

}
