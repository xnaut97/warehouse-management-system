package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingAccuracyReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingAccuracyResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingAccuracyTotalsResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReasonResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReasonRow;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceValueResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingWarehouseVarianceResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingWarehouseVarianceRow;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.repository.report.StocktakingReportRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StocktakingAccuracyReportService {

    private static final int DEFAULT_PERIOD_MONTHS = 12;

    private static final int SCALE = 2;

    private static final int MAX_REASONS = 10;

    private static final Set<StocktakingStatus> COMPLETED_STATUSES = Set.of(
            StocktakingStatus.COUNT_CONFIRMED,
            StocktakingStatus.STOCK_BALANCED
    );

    private final StocktakingReportRepository repository;

    @Transactional(readOnly = true)
    public StocktakingAccuracyReportResponse getReport(
            LocalDate fromDate,
            LocalDate toDate,
            Long warehouseId
    ) {

        LocalDate resolvedToDate =
                toDate != null
                        ? toDate
                        : LocalDate.now();

        LocalDate resolvedFromDate =
                fromDate != null && !fromDate.isAfter(resolvedToDate)
                        ? fromDate
                        : YearMonth.from(resolvedToDate)
                        .minusMonths(DEFAULT_PERIOD_MONTHS - 1L)
                        .atDay(1);

        StocktakingAccuracyTotalsResponse totals = repository.getAccuracyTotals(
                COMPLETED_STATUSES,
                StocktakingItemStatus.DISCREPANCY,
                resolvedFromDate,
                resolvedToDate,
                warehouseId
        );

        long completedStocktakings = repository.countCompletedStocktakings(
                COMPLETED_STATUSES,
                resolvedFromDate,
                resolvedToDate,
                warehouseId
        );

        return StocktakingAccuracyReportResponse.builder()

                .fromDate(resolvedFromDate)

                .toDate(resolvedToDate)

                .accuracy(
                        buildAccuracy(totals, completedStocktakings)
                )

                .varianceValue(
                        buildVarianceValue(totals)
                )

                .warehouses(
                        buildWarehouses(
                                resolvedFromDate,
                                resolvedToDate,
                                warehouseId
                        )
                )

                .reasons(
                        buildReasons(
                                resolvedFromDate,
                                resolvedToDate,
                                warehouseId
                        )
                )

                .build();

    }

    private StocktakingAccuracyResponse buildAccuracy(
            StocktakingAccuracyTotalsResponse totals,
            long completedStocktakings
    ) {

        BigDecimal systemQuantity =
                totals == null
                        ? BigDecimal.ZERO
                        : orZero(totals.getTotalSystemQuantity());

        BigDecimal physicalQuantity =
                totals == null
                        ? BigDecimal.ZERO
                        : orZero(totals.getTotalPhysicalQuantity());

        long totalItems =
                totals == null || totals.getTotalItems() == null
                        ? 0
                        : totals.getTotalItems();

        long discrepancyItems =
                totals == null || totals.getDiscrepancyItems() == null
                        ? 0
                        : totals.getDiscrepancyItems();

        if (totalItems == 0) {

            return StocktakingAccuracyResponse.builder()
                    .available(false)
                    .unavailableReason("NO_COMPLETED_STOCKTAKING")
                    .totalSystemQuantity(scaled(systemQuantity))
                    .totalPhysicalQuantity(scaled(physicalQuantity))
                    .totalItems(totalItems)
                    .discrepancyItems(discrepancyItems)
                    .completedStocktakings(completedStocktakings)
                    .build();

        }

        return StocktakingAccuracyResponse.builder()
                .available(true)
                .accuracyPercent(
                        BigDecimal.valueOf(totalItems - discrepancyItems)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        BigDecimal.valueOf(totalItems),
                                        SCALE,
                                        RoundingMode.HALF_UP
                                )
                )
                .totalSystemQuantity(scaled(systemQuantity))
                .totalPhysicalQuantity(scaled(physicalQuantity))
                .totalItems(totalItems)
                .discrepancyItems(discrepancyItems)
                .completedStocktakings(completedStocktakings)
                .build();
    }

    private StocktakingVarianceValueResponse buildVarianceValue(
            StocktakingAccuracyTotalsResponse totals
    ) {

        if (totals == null) {

            return StocktakingVarianceValueResponse.builder()
                    .netVarianceValue(BigDecimal.ZERO)
                    .absoluteVarianceValue(BigDecimal.ZERO)
                    .netVarianceQuantity(BigDecimal.ZERO)
                    .absoluteVarianceQuantity(BigDecimal.ZERO)
                    .build();

        }

        return StocktakingVarianceValueResponse.builder()
                .netVarianceValue(scaled(totals.getNetVarianceValue()))
                .absoluteVarianceValue(scaled(totals.getAbsoluteVarianceValue()))
                .netVarianceQuantity(scaled(totals.getNetVarianceQuantity()))
                .absoluteVarianceQuantity(
                        scaled(totals.getAbsoluteVarianceQuantity())
                )
                .build();

    }

    private List<StocktakingWarehouseVarianceResponse> buildWarehouses(
            LocalDate fromDate,
            LocalDate toDate,
            Long warehouseId
    ) {

        List<StocktakingWarehouseVarianceRow> rows =
                repository.getVarianceByWarehouse(
                        COMPLETED_STATUSES,
                        fromDate,
                        toDate,
                        warehouseId
                );

        BigDecimal total = rows.stream()
                .map(row -> orZero(row.getAbsoluteVarianceValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StocktakingWarehouseVarianceResponse> result = new ArrayList<>();

        rows.forEach(row -> result.add(
                StocktakingWarehouseVarianceResponse.builder()
                        .warehouseId(row.getWarehouseId())
                        .warehouseCode(row.getWarehouseCode())
                        .warehouseName(row.getWarehouseName())
                        .group(resolveGroup(row))
                        .itemCount(
                                row.getItemCount() == null
                                        ? 0
                                        : row.getItemCount()
                        )
                        .netVarianceQuantity(
                                scaled(row.getNetVarianceQuantity())
                        )
                        .absoluteVarianceQuantity(
                                scaled(row.getAbsoluteVarianceQuantity())
                        )
                        .netVarianceValue(
                                scaled(row.getNetVarianceValue())
                        )
                        .absoluteVarianceValue(
                                scaled(row.getAbsoluteVarianceValue())
                        )
                        .sharePercent(
                                percentage(
                                        orZero(row.getAbsoluteVarianceValue()),
                                        total
                                )
                        )
                        .build()
        ));

        return result;

    }

    private List<StocktakingReasonResponse> buildReasons(
            LocalDate fromDate,
            LocalDate toDate,
            Long warehouseId
    ) {

        Map<String, StocktakingReasonResponse> grouped = new LinkedHashMap<>();

        mergeReasons(grouped, repository.getMaterialItemReasons(
                COMPLETED_STATUSES,
                fromDate,
                toDate,
                warehouseId
        ));

        mergeReasons(grouped, repository.getProductBatchReasons(
                COMPLETED_STATUSES,
                fromDate,
                toDate,
                warehouseId
        ));

        return grouped.values().stream()
                .sorted(Comparator
                        .comparing(StocktakingReasonResponse::getItemCount)
                        .thenComparing(
                                StocktakingReasonResponse::getAbsoluteVarianceValue
                        )
                        .reversed())
                .limit(MAX_REASONS)
                .toList();

    }

    private void mergeReasons(
            Map<String, StocktakingReasonResponse> target,
            List<StocktakingReasonRow> rows
    ) {

        rows.forEach(row -> {

            String reason =
                    row.getReason() == null || row.getReason().isBlank()
                            ? null
                            : row.getReason().trim();

            String key = reason == null
                    ? ""
                    : reason;

            StocktakingReasonResponse current = target.get(key);

            long itemCount = row.getItemCount() == null
                    ? 0
                    : row.getItemCount();

            BigDecimal quantity = orZero(row.getAbsoluteVarianceQuantity());

            BigDecimal value = orZero(row.getAbsoluteVarianceValue());

            if (current != null) {

                itemCount += current.getItemCount();

                quantity = quantity.add(
                        current.getAbsoluteVarianceQuantity()
                );

                value = value.add(current.getAbsoluteVarianceValue());

            }

            target.put(key, StocktakingReasonResponse.builder()
                    .reason(reason)
                    .unspecified(reason == null)
                    .itemCount(itemCount)
                    .absoluteVarianceQuantity(scaled(quantity))
                    .absoluteVarianceValue(scaled(value))
                    .build());

        });

    }

    private StockGroup resolveGroup(StocktakingWarehouseVarianceRow row) {

        boolean hasProductItems =
                row.getProductItemCount() != null
                        && row.getProductItemCount() > 0;

        if (hasProductItems) {
            return StockGroup.PRODUCT;
        }

        return WarehouseService.PRODUCT_WAREHOUSE_CODE
                .equals(row.getWarehouseCode())
                ? StockGroup.PRODUCT
                : StockGroup.MATERIAL;

    }

    private BigDecimal percentage(
            BigDecimal value,
            BigDecimal total
    ) {

        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return value
                .multiply(BigDecimal.valueOf(100))
                .divide(total, SCALE, RoundingMode.HALF_UP);

    }

    private BigDecimal scaled(BigDecimal value) {

        return orZero(value).setScale(SCALE, RoundingMode.HALF_UP);

    }

    private BigDecimal orZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;

    }

}
