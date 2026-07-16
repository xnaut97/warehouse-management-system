package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.dashboard.*;
import com.github.xnaut97.wms.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/summary")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<DashboardSummaryResponse> summary() {

        return ApiResponse.success(
                "Dashboard retrieved successfully",
                service.summary()
        );

    }

    @GetMapping("/overview")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<DashboardOverviewResponse> overview() {

        return ApiResponse.success(
                "Dashboard overview retrieved successfully",
                service.overview()
        );

    }

    @GetMapping("/monthly-receipts")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<MonthlyStatisticResponse>> monthlyReceipts() {

        return ApiResponse.success(
                "Monthly receipts retrieved successfully",
                service.monthlyReceipts()
        );

    }

    @GetMapping("/monthly-issues")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<MonthlyStatisticResponse>> monthlyIssues() {

        return ApiResponse.success(
                "Monthly issues retrieved successfully",
                service.monthlyIssues()
        );

    }

    @GetMapping("/inventory-analysis")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<InventoryAnalysisResponse> inventoryAnalysis() {

        return ApiResponse.success(
                "Inventory analysis retrieved successfully",
                service.inventoryAnalysis()
        );

    }

    @GetMapping("/inventory-variance")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<InventoryVarianceResponse> inventoryVariance() {

        return ApiResponse.success(
                "Inventory variance dashboard retrieved successfully",
                service.inventoryVariance()
        );

    }

    @GetMapping("/decision-support")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<DecisionSupportResponse> decisionSupport() {

        return ApiResponse.success(
                "Decision support dashboard retrieved successfully",
                service.decisionSupport()
        );

    }

    @GetMapping("/low-stock-alerts")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<LowStockAlertResponse>>
    lowStockAlerts() {

        return ApiResponse.success(
                "Low stock alerts retrieved successfully",
                service.lowStockAlerts()
        );

    }

    @GetMapping("/replenishment-recommendations")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<ReplenishmentRecommendationResponse>> replenishmentRecommendations() {

        return ApiResponse.success(

                "Replenishment recommendations retrieved successfully",

                service.replenishmentRecommendations()

        );

    }

    @GetMapping("/inventory-trend")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD'
            )
            """)
    public ApiResponse<List<InventoryTrendResponse>> inventoryTrend() {

        return ApiResponse.success(

                "Inventory trend retrieved successfully",

                service.inventoryTrend()

        );

    }
}
