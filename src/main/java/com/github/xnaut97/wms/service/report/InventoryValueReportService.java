package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.value.InventoryTurnoverResponse;
import com.github.xnaut97.wms.dto.report.value.InventoryValueGroupResponse;
import com.github.xnaut97.wms.dto.report.value.InventoryValueMonthlyResponse;
import com.github.xnaut97.wms.dto.report.value.InventoryValueReportResponse;
import com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.report.InventoryValueReportRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryValueReportService {

    private static final int TREND_MONTHS = 12;

    private static final int SCALE = 2;

    private final InventoryValueReportRepository repository;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final ProductInventoryRepository productInventoryRepository;

    @Transactional(readOnly = true)
    public InventoryValueReportResponse getReport(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        LocalDate resolvedToDate =
                toDate != null
                        ? toDate
                        : LocalDate.now();

        LocalDate resolvedFromDate =
                fromDate != null && !fromDate.isAfter(resolvedToDate)
                        ? fromDate
                        : YearMonth.from(resolvedToDate)
                        .minusMonths(TREND_MONTHS - 1L)
                        .atDay(1);

        BigDecimal materialValue =
                orZero(materialInventoryRepository.getTotalInventoryValue());

        BigDecimal productValue =
                orZero(productInventoryRepository.getTotalInventoryValue());

        BigDecimal totalValue = materialValue.add(productValue);

        List<InventoryValueMonthlyResponse> monthlyTrend =
                buildMonthlyTrend(materialValue, productValue);

        return InventoryValueReportResponse.builder()

                .fromDate(resolvedFromDate)

                .toDate(resolvedToDate)

                .totalInventoryValue(totalValue)

                .materialValue(materialValue)

                .productValue(productValue)

                .materialPercentage(
                        percentage(materialValue, totalValue)
                )

                .productPercentage(
                        percentage(productValue, totalValue)
                )

                .groups(
                        buildGroups(materialValue, productValue, totalValue)
                )

                .monthlyTrend(monthlyTrend)

                .turnover(
                        buildTurnover(
                                resolvedFromDate,
                                resolvedToDate,
                                monthlyTrend
                        )
                )

                .build();

    }

    private List<InventoryValueGroupResponse> buildGroups(
            BigDecimal materialValue,
            BigDecimal productValue,
            BigDecimal totalValue
    ) {

        return List.of(

                InventoryValueGroupResponse.builder()
                        .group(StockGroup.MATERIAL)
                        .warehouseCode(WarehouseService.MATERIAL_WAREHOUSE_CODE)
                        .value(materialValue)
                        .percentage(percentage(materialValue, totalValue))
                        .build(),

                InventoryValueGroupResponse.builder()
                        .group(StockGroup.PRODUCT)
                        .warehouseCode(WarehouseService.PRODUCT_WAREHOUSE_CODE)
                        .value(productValue)
                        .percentage(percentage(productValue, totalValue))
                        .build()

        );

    }

    private List<InventoryValueMonthlyResponse> buildMonthlyTrend(
            BigDecimal materialValue,
            BigDecimal productValue
    ) {

        YearMonth currentMonth = YearMonth.now();

        YearMonth startMonth = currentMonth.minusMonths(TREND_MONTHS - 1L);

        LocalDate movementFrom = startMonth.atDay(1);

        Map<YearMonth, BigDecimal> materialIn =
                toMonthlyMap(repository.getMonthlyMaterialInValue(
                        ReceiptStatus.CONFIRMED,
                        movementFrom
                ));

        Map<YearMonth, BigDecimal> materialOut =
                toMonthlyMap(repository.getMonthlyMaterialOutValue(
                        IssueStatus.CONFIRMED,
                        movementFrom
                ));

        Map<YearMonth, BigDecimal> materialAdjustment =
                toMonthlyMap(repository.getMonthlyMaterialAdjustmentValue(
                        StocktakingStatus.STOCK_BALANCED,
                        movementFrom
                ));

        Map<YearMonth, BigDecimal> productIn =
                toMonthlyMap(repository.getMonthlyProductInValue(
                        ReceiptStatus.CONFIRMED,
                        movementFrom
                ));

        Map<YearMonth, BigDecimal> productOut =
                toMonthlyMap(repository.getMonthlyProductOutValue(
                        IssueStatus.CONFIRMED,
                        movementFrom
                ));

        Map<YearMonth, BigDecimal> productAdjustment =
                toMonthlyMap(repository.getMonthlyProductAdjustmentValue(
                        StocktakingStatus.STOCK_BALANCED,
                        movementFrom
                ));

        InventoryValueMonthlyResponse[] series =
                new InventoryValueMonthlyResponse[TREND_MONTHS];

        BigDecimal materialClosing = materialValue;

        BigDecimal productClosing = productValue;

        for (int index = TREND_MONTHS - 1; index >= 0; index--) {

            YearMonth month = startMonth.plusMonths(index);

            series[index] = InventoryValueMonthlyResponse.builder()
                    .month(month.toString())
                    .materialValue(scaled(materialClosing))
                    .productValue(scaled(productClosing))
                    .totalValue(scaled(materialClosing.add(productClosing)))
                    .build();

            materialClosing = materialClosing
                    .subtract(valueOf(materialIn, month))
                    .add(valueOf(materialOut, month))
                    .subtract(valueOf(materialAdjustment, month));

            productClosing = productClosing
                    .subtract(valueOf(productIn, month))
                    .add(valueOf(productOut, month))
                    .subtract(valueOf(productAdjustment, month));

        }

        return new ArrayList<>(List.of(series));

    }

    private InventoryTurnoverResponse buildTurnover(
            LocalDate fromDate,
            LocalDate toDate,
            List<InventoryValueMonthlyResponse> monthlyTrend
    ) {

        BigDecimal costOfGoodsIssued =
                orZero(repository.getMaterialIssuedValue(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                )).add(orZero(repository.getProductIssuedValue(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                )));

        YearMonth fromMonth = YearMonth.from(fromDate);

        YearMonth toMonth = YearMonth.from(toDate);

        List<BigDecimal> valuesInPeriod = monthlyTrend.stream()
                .filter(item -> isWithin(
                        YearMonth.parse(item.getMonth()),
                        fromMonth,
                        toMonth
                ))
                .map(InventoryValueMonthlyResponse::getTotalValue)
                .toList();

        int periodMonths = (int) ChronoUnit.MONTHS.between(
                fromMonth,
                toMonth
        ) + 1;

        if (valuesInPeriod.isEmpty()) {

            return InventoryTurnoverResponse.builder()
                    .available(false)
                    .unavailableReason("NO_INVENTORY_VALUE_IN_PERIOD")
                    .costOfGoodsIssued(scaled(costOfGoodsIssued))
                    .averageInventoryValue(BigDecimal.ZERO)
                    .periodMonths(periodMonths)
                    .build();

        }

        BigDecimal averageInventoryValue = valuesInPeriod.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(
                        BigDecimal.valueOf(valuesInPeriod.size()),
                        SCALE,
                        RoundingMode.HALF_UP
                );

        if (averageInventoryValue.compareTo(BigDecimal.ZERO) <= 0) {

            return InventoryTurnoverResponse.builder()
                    .available(false)
                    .unavailableReason("ZERO_AVERAGE_INVENTORY")
                    .costOfGoodsIssued(scaled(costOfGoodsIssued))
                    .averageInventoryValue(scaled(averageInventoryValue))
                    .periodMonths(periodMonths)
                    .build();

        }

        return InventoryTurnoverResponse.builder()
                .available(true)
                .ratio(
                        costOfGoodsIssued.divide(
                                averageInventoryValue,
                                SCALE,
                                RoundingMode.HALF_UP
                        )
                )
                .costOfGoodsIssued(scaled(costOfGoodsIssued))
                .averageInventoryValue(averageInventoryValue)
                .periodMonths(periodMonths)
                .build();

    }

    private boolean isWithin(
            YearMonth month,
            YearMonth fromMonth,
            YearMonth toMonth
    ) {

        return !month.isBefore(fromMonth) && !month.isAfter(toMonth);

    }

    private Map<YearMonth, BigDecimal> toMonthlyMap(
            List<MonthlyValueResponse> rows
    ) {

        Map<YearMonth, BigDecimal> result = new HashMap<>();

        rows.forEach(row -> result.merge(
                YearMonth.of(row.getYear(), row.getMonth()),
                orZero(row.getValue()),
                BigDecimal::add
        ));

        return result;

    }

    private BigDecimal valueOf(
            Map<YearMonth, BigDecimal> source,
            YearMonth month
    ) {

        return source.getOrDefault(month, BigDecimal.ZERO);

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
