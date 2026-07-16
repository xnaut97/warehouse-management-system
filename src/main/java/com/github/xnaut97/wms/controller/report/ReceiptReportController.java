package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptDailyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptMonthlyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptSupplierReportResponse;
import com.github.xnaut97.wms.service.report.ReceiptReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/receipts")
@RequiredArgsConstructor
public class ReceiptReportController {

    private final ReceiptReportService service;

    @GetMapping("/daily")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'WAREHOUSE_MANAGER',
                'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<ReceiptDailyReportResponse> dailyReceiptReport(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date

    ) {

        return ApiResponse.success(
                "Daily receipt report retrieved successfully",
                service.getDailyReceiptReport(date)
        );

    }

    @GetMapping("/monthly")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'WAREHOUSE_MANAGER',
                'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<ReceiptMonthlyReportResponse>> monthlyReceiptReport(

            @RequestParam
            Integer year

    ) {

        return ApiResponse.success(
                "Monthly receipt report retrieved successfully",
                service.getMonthlyReceiptReport(year)
        );

    }

    @GetMapping("/suppliers")
    @PreAuthorize("""
            hasAnyRole(
                'ADMIN',
                'WAREHOUSE_MANAGER',
                'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<ReceiptSupplierReportResponse>> supplierReceiptReport(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate

    ) {

        return ApiResponse.success(
                "Supplier receipt report retrieved successfully",
                service.getSupplierReceiptReport(fromDate, toDate)
        );

    }

}