package com.github.xnaut97.wms.controller;

import com.github.xnaut97.wms.dto.common.ApiResponse;
import com.github.xnaut97.wms.dto.statistic.StatisticResponse;
import com.github.xnaut97.wms.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService service;

    @GetMapping
    public ApiResponse<StatisticResponse> statistics() {

        return ApiResponse.success(
                "Statistics retrieved successfully",
                service.getStatistics()
        );

    }

}
