package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse;
import com.github.xnaut97.wms.repository.report.StocktakingReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakingReportService {

    private final StocktakingReportRepository repository;

    @Transactional
    public List<StocktakingReportResponse> getStocktakingReport() {

        return repository.getStocktakingReport();

    }

    @Transactional
    public List<StocktakingVarianceReportResponse> getVarianceReport() {

        return repository.getVarianceReport();

    }

    @Transactional
    public StocktakingSummaryReportResponse getSummary() {

        return repository.getSummary();

    }
}
