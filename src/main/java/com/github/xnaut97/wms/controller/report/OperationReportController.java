package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationReportResponse;
import com.github.xnaut97.wms.dto.report.operation.StockSummaryReportResponse;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.service.report.OperationReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports/operations")
@RequiredArgsConstructor
public class OperationReportController {

    private final OperationReportService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD',
            'ACCOUNTANT'
            )
            """)
    public ApiResponse<OperationReportResponse> report(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate

    ) {

        return ApiResponse.success(

                "Operation report retrieved successfully",

                service.getReport(fromDate, toDate)

        );

    }

    @GetMapping("/stock-summary")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD',
            'ACCOUNTANT'
            )
            """)
    public ApiResponse<StockSummaryReportResponse> stockSummary(

            @RequestParam(required = false)
            StockGroup stockGroup,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate

    ) {

        return ApiResponse.success(

                "Stock summary report retrieved successfully",

                service.getStockSummary(stockGroup, fromDate, toDate)

        );

    }

}
