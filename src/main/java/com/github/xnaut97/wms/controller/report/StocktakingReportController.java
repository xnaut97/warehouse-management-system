package com.github.xnaut97.wms.controller.report;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse;
import com.github.xnaut97.wms.service.report.StocktakingReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/stocktaking")
@RequiredArgsConstructor
public class StocktakingReportController {

    private final StocktakingReportService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<Page<StocktakingReportResponse>> report(
            Pageable pageable
    ) {

        return ApiResponse.success(

                "Stocktaking report retrieved successfully",

                service.getStocktakingReport(pageable)

        );

    }

    @GetMapping("/variances")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<Page<StocktakingVarianceReportResponse>> variances(
            Pageable pageable
    ) {

        return ApiResponse.success(

                "Variance report retrieved successfully",

                service.getVarianceReport(pageable)

        );

    }

    @GetMapping("/summary")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<StocktakingSummaryReportResponse> summary() {

        return ApiResponse.success(

                "Summary retrieved successfully",

                service.getSummary()

        );

    }
}
