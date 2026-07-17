package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse;
import com.github.xnaut97.wms.repository.report.StocktakingReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StocktakingReportService {

    private final StocktakingReportRepository repository;

    @Transactional
    public Page<StocktakingReportResponse> getStocktakingReport(
            Pageable pageable
    ) {

        return repository.getStocktakingReport(pageable);

    }

    @Transactional
    public Page<StocktakingVarianceReportResponse> getVarianceReport(
            Pageable pageable
    ) {

        return repository.getVarianceReport(pageable);

    }

    @Transactional
    public StocktakingSummaryReportResponse getSummary() {

        return repository.getSummary();

    }
}
