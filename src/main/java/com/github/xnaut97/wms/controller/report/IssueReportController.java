package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueCustomerReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueDailyReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueMaterialReportResponse;
import com.github.xnaut97.wms.service.report.IssueReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports/issues")
@RequiredArgsConstructor
public class IssueReportController {

    private final IssueReportService service;

    @GetMapping("/daily")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<IssueDailyReportResponse> dailyIssueReport(

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date

    ) {

        return ApiResponse.success(
                "Daily issue report retrieved successfully",
                service.getDailyIssueReport(date)
        );

    }

    @GetMapping("/customers")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<IssueCustomerReportResponse>> customerIssueReport(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate

    ) {

        return ApiResponse.success(
                "Customer issue report retrieved successfully",
                service.getCustomerIssueReport(
                        fromDate,
                        toDate
                )
        );

    }

    @GetMapping("/materials")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<IssueMaterialReportResponse>> materialIssueReport(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate

    ) {

        return ApiResponse.success(
                "Material issue report retrieved successfully",
                service.getMaterialIssueReport(
                        fromDate,
                        toDate
                )
        );

    }


}