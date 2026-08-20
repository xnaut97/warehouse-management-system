package com.github.xnaut97.wms.controller.alert;

import com.github.xnaut97.wms.dto.alert.AlertCenterResponse;
import com.github.xnaut97.wms.dto.alert.AlertLotOverviewResponse;
import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.enums.AlertType;
import com.github.xnaut97.wms.service.alert.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService service;

    @GetMapping
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD',
            'ACCOUNTANT'
            )
            """)
    public ApiResponse<AlertCenterResponse> alerts(

            @RequestParam(required = false)
            Long warehouseId,

            @RequestParam(required = false)
            AlertType type

    ) {

        return ApiResponse.success(

                "Alerts retrieved successfully",

                service.getAlerts(warehouseId, type)

        );

    }

    @GetMapping("/lots")
    @PreAuthorize("""
            hasAnyRole(
            'ADMIN',
            'WAREHOUSE_MANAGER',
            'EXECUTIVE_BOARD',
            'ACCOUNTANT'
            )
            """)
    public ApiResponse<AlertLotOverviewResponse> lots(

            @RequestParam(required = false)
            Long warehouseId

    ) {

        return ApiResponse.success(

                "Lot overview retrieved successfully",

                service.getLotOverview(warehouseId)

        );

    }

}
