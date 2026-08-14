package com.github.xnaut97.wms.service.alert;

import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.dto.alert.*;
import com.github.xnaut97.wms.enums.*;
import com.github.xnaut97.wms.repository.alert.AlertRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private static final int NEAR_EXPIRY_CRITICAL_DAYS = 60;

    private static final int NEAR_EXPIRY_WARNING_DAYS = 90;

    private static final int TOP_NEAR_EXPIRY_LIMIT = 20;

    private final AlertRepository repository;

    @Audit(
            action = AuditAction.READ,
            entity = "Alert"
    )
    @Transactional
    public AlertCenterResponse getAlerts(
            Long warehouseId,
            AlertType type
    ) {

        LocalDate today = LocalDate.now();

        List<AlertItemResponse> alerts = new ArrayList<>();

        alerts.addAll(thresholdAlerts(warehouseId));

        alerts.addAll(expiryAlerts(warehouseId, today));

        alerts.addAll(varianceAlerts(warehouseId));

        List<AlertItemResponse> filtered = alerts.stream()
                .filter(alert -> type == null || alert.getType() == type)
                .sorted(
                        Comparator
                                .comparing((AlertItemResponse alert) ->
                                        alert.getSeverity() == AlertSeverity.CRITICAL ? 0 : 1)
                                .thenComparing(AlertItemResponse::getType)
                                .thenComparing(
                                        AlertItemResponse::getItemCode,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                )
                .toList();

        long critical = filtered.stream()
                .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL)
                .count();

        return AlertCenterResponse.builder()
                .summary(
                        new AlertSummaryResponse(
                                filtered.size(),
                                critical,
                                filtered.size() - critical
                        )
                )
                .alerts(filtered)
                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Alert"
    )
    @Transactional
    public AlertLotOverviewResponse getLotOverview(Long warehouseId) {

        LocalDate today = LocalDate.now();

        List<NearExpiryLotResponse> lotViews = allLotViews(warehouseId, today);

        long redAlertLots = lotViews.stream()
                .filter(lot -> lot.getDaysToExpiry() != null
                        && lot.getDaysToExpiry() <= NEAR_EXPIRY_CRITICAL_DAYS)
                .count();

        List<NearExpiryLotResponse> expired = lotViews.stream()
                .filter(lot -> lot.getExpirationDate() != null
                        && lot.getExpirationDate().isBefore(today))
                .toList();

        BigDecimal expiredValue = expired.stream()
                .map(NearExpiryLotResponse::getStockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AlertLotKpiResponse kpi = AlertLotKpiResponse.builder()
                .totalLotsInStock(lotViews.size())
                .redAlertLots(redAlertLots)
                .expiredLots(expired.size())
                .expiredLotValue(expiredValue)
                .lotsWithoutExpiry(
                        lotViews.stream()
                                .filter(lot -> lot.getExpirationDate() == null)
                                .count()
                )
                .build();

        List<NearExpiryLotResponse> topNearExpiry = lotViews.stream()
                .filter(lot -> lot.getExpirationDate() != null)
                .sorted(Comparator.comparing(NearExpiryLotResponse::getExpirationDate))
                .limit(TOP_NEAR_EXPIRY_LIMIT)
                .toList();

        return AlertLotOverviewResponse.builder()
                .kpi(kpi)
                .expiryDistribution(expiryDistribution(lotViews))
                .topNearExpiryLots(topNearExpiry)
                .build();

    }

    private List<AlertItemResponse> thresholdAlerts(Long warehouseId) {

        List<AlertItemResponse> alerts = new ArrayList<>();

        repository.findMaterialThresholdRows(WarehouseService.STORAGE_AREA_CODES, warehouseId)
                .forEach(row ->
                        alerts.add(toThresholdAlert(row, StockGroup.MATERIAL))
                );

        repository.findProductThresholdRows(WarehouseService.STORAGE_AREA_CODES, warehouseId)
                .forEach(row ->
                        alerts.add(toThresholdAlert(row, StockGroup.PRODUCT))
                );

        return alerts;

    }

    private AlertItemResponse toThresholdAlert(
            AlertStockRow row,
            StockGroup group
    ) {

        BigDecimal quantity = orZero(row.getTotalQuantity());

        BigDecimal minimum = orZero(row.getMinimumStock());

        boolean belowMin = quantity.compareTo(minimum) < 0;

        AlertType type = belowMin
                ? AlertType.BELOW_MIN
                : AlertType.ABOVE_MAX;

        AlertSeverity severity =
                belowMin && quantity.compareTo(BigDecimal.ZERO) == 0
                        ? AlertSeverity.CRITICAL
                        : AlertSeverity.WARNING;

        String detail = belowMin
                ? String.format(
                        "Tồn %s %s / Định mức tối thiểu %s %s (thiếu %s %s)",
                        plain(quantity),
                        row.getUnit(),
                        plain(minimum),
                        row.getUnit(),
                        plain(minimum.subtract(quantity)),
                        row.getUnit()
                )
                : String.format(
                        "Tồn %s %s / Định mức tối đa %s %s (vượt %s %s)",
                        plain(quantity),
                        row.getUnit(),
                        plain(orZero(row.getMaximumStock())),
                        row.getUnit(),
                        plain(quantity.subtract(orZero(row.getMaximumStock()))),
                        row.getUnit()
                );

        return AlertItemResponse.builder()
                .severity(severity)
                .type(type)
                .riskGroup(type.getRiskGroup())
                .group(group)
                .itemCode(row.getItemCode())
                .itemName(row.getItemName())
                .warehouseId(row.getWarehouseId())
                .warehouseCode(row.getWarehouseCode())
                .warehouseName(row.getWarehouseName())
                .detail(detail)
                .lotTracked(false)
                .build();

    }

    private List<AlertItemResponse> expiryAlerts(
            Long warehouseId,
            LocalDate today
    ) {

        return allLotViews(warehouseId, today).stream()
                .filter(lot -> lot.getDaysToExpiry() != null)
                .filter(lot -> lot.getDaysToExpiry() <= NEAR_EXPIRY_WARNING_DAYS)
                .map(this::toExpiryAlert)
                .toList();

    }

    private AlertItemResponse toExpiryAlert(NearExpiryLotResponse lot) {

        long days = lot.getDaysToExpiry();

        AlertSeverity severity = days <= NEAR_EXPIRY_CRITICAL_DAYS
                ? AlertSeverity.CRITICAL
                : AlertSeverity.WARNING;

        String detail = days < 0
                ? String.format(
                        "Đã quá hạn %s ngày, còn tồn %s %s - không được phép xuất",
                        Math.abs(days),
                        plain(lot.getLotQuantity()),
                        lot.getUnit()
                )
                : String.format(
                        "Còn %s ngày đến hạn, tồn lô %s %s",
                        days,
                        plain(lot.getLotQuantity()),
                        lot.getUnit()
                );

        return AlertItemResponse.builder()
                .severity(severity)
                .type(AlertType.NEAR_EXPIRY)
                .riskGroup(AlertType.NEAR_EXPIRY.getRiskGroup())
                .group(lot.getGroup())
                .itemCode(lot.getItemCode())
                .itemName(lot.getItemName())
                .warehouseId(lot.getWarehouseId())
                .warehouseCode(lot.getWarehouseCode())
                .warehouseName(lot.getWarehouseName())
                .lotNumber(lot.getLotNumber())
                .expirationDate(lot.getExpirationDate())
                .daysToExpiry(days)
                .detail(detail)
                .lotTracked(lot.isLotTracked())
                .build();

    }

    private List<AlertItemResponse> varianceAlerts(Long warehouseId) {

        List<AlertItemResponse> alerts = new ArrayList<>();

        repository.findMaterialVarianceRows(
                        WarehouseService.STORAGE_AREA_CODES,
                        StocktakingStatus.STOCK_BALANCED,
                        StocktakingItemStatus.DISCREPANCY,
                        warehouseId
                )
                .forEach(row ->
                        alerts.add(toVarianceAlert(row, StockGroup.MATERIAL))
                );

        repository.findProductBatchVarianceRows(
                        WarehouseService.STORAGE_AREA_CODES,
                        StocktakingStatus.STOCK_BALANCED,
                        warehouseId
                )
                .forEach(row ->
                        alerts.add(toVarianceAlert(row, StockGroup.PRODUCT))
                );

        return alerts;

    }

    private AlertItemResponse toVarianceAlert(
            AlertVarianceRow row,
            StockGroup group
    ) {

        boolean counted =
                row.getStocktakingStatus() == StocktakingStatus.COUNT_CONFIRMED;

        BigDecimal variance = orZero(row.getVarianceQuantity());

        String direction = variance.compareTo(BigDecimal.ZERO) > 0
                ? "thừa"
                : "thiếu";

        String detail = String.format(
                "Phiếu %s: sổ sách %s %s, thực tế %s %s, %s %s %s - %s",
                row.getStocktakingNo(),
                plain(orZero(row.getSystemQuantity())),
                row.getUnit(),
                plain(orZero(row.getPhysicalQuantity())),
                row.getUnit(),
                direction,
                plain(variance.abs()),
                row.getUnit(),
                counted
                        ? "đã chốt số lượng thực tế, chờ cân bằng tồn kho"
                        : "đang kiểm kê"
        );

        return AlertItemResponse.builder()
                .severity(
                        counted
                                ? AlertSeverity.CRITICAL
                                : AlertSeverity.WARNING
                )
                .type(AlertType.STOCKTAKING_VARIANCE)
                .riskGroup(AlertType.STOCKTAKING_VARIANCE.getRiskGroup())
                .group(group)
                .itemCode(row.getItemCode())
                .itemName(row.getItemName())
                .warehouseId(row.getWarehouseId())
                .warehouseCode(row.getWarehouseCode())
                .warehouseName(row.getWarehouseName())
                .lotNumber(row.getLotNumber())
                .expirationDate(row.getExpirationDate())
                .detail(detail)
                .lotTracked(group == StockGroup.PRODUCT)
                .build();

    }

    private List<NearExpiryLotResponse> allLotViews(
            Long warehouseId,
            LocalDate today
    ) {

        List<NearExpiryLotResponse> lots = new ArrayList<>();

        repository.findProductLotRows(WarehouseService.STORAGE_AREA_CODES, warehouseId)
                .forEach(row ->
                        lots.add(toLotView(row, StockGroup.PRODUCT, today))
                );

        repository.findMaterialLotRows(
                        WarehouseService.STORAGE_AREA_CODES,
                        ReceiptStatus.CONFIRMED,
                        warehouseId
                )
                .forEach(row ->
                        lots.add(toLotView(row, StockGroup.MATERIAL, today))
                );

        return lots;

    }

    private NearExpiryLotResponse toLotView(
            AlertLotRow row,
            StockGroup group,
            LocalDate today
    ) {

        BigDecimal quantity = orZero(row.getQuantity());

        BigDecimal price = orZero(row.getAveragePrice());

        return NearExpiryLotResponse.builder()
                .group(group)
                .itemCode(row.getItemCode())
                .itemName(row.getItemName())
                .unit(row.getUnit())
                .warehouseId(row.getWarehouseId())
                .warehouseCode(row.getWarehouseCode())
                .warehouseName(row.getWarehouseName())
                .lotNumber(row.getLotNumber())
                .expirationDate(row.getExpirationDate())
                .daysToExpiry(
                        row.getExpirationDate() == null
                                ? null
                                : ChronoUnit.DAYS.between(
                                        today,
                                        row.getExpirationDate()
                                )
                )
                .lotQuantity(quantity)
                .averagePrice(price)
                .stockValue(quantity.multiply(price))
                .lotTracked(group == StockGroup.PRODUCT)
                .build();

    }

    private List<ExpiryBucketResponse> expiryDistribution(
            List<NearExpiryLotResponse> lots
    ) {

        return List.of(
                bucket(lots, "LT_30", "< 30 ngày", Long.MIN_VALUE, 30),
                bucket(lots, "D30_60", "30 - 60 ngày", 30, 60),
                bucket(lots, "D60_90", "60 - 90 ngày", 60, 90),
                bucket(lots, "GT_90", "> 90 ngày", 90, Long.MAX_VALUE)
        );

    }

    private ExpiryBucketResponse bucket(
            List<NearExpiryLotResponse> lots,
            String bucket,
            String label,
            long fromInclusive,
            long toExclusive
    ) {

        List<NearExpiryLotResponse> matched = lots.stream()
                .filter(lot -> lot.getDaysToExpiry() != null)
                .filter(lot -> lot.getDaysToExpiry() >= fromInclusive
                        && lot.getDaysToExpiry() < toExclusive)
                .toList();

        return ExpiryBucketResponse.builder()
                .bucket(bucket)
                .label(label)
                .materialLots(count(matched, StockGroup.MATERIAL))
                .productLots(count(matched, StockGroup.PRODUCT))
                .materialQuantity(
                        sum(matched, StockGroup.MATERIAL, NearExpiryLotResponse::getLotQuantity)
                )
                .productQuantity(
                        sum(matched, StockGroup.PRODUCT, NearExpiryLotResponse::getLotQuantity)
                )
                .materialValue(
                        sum(matched, StockGroup.MATERIAL, NearExpiryLotResponse::getStockValue)
                )
                .productValue(
                        sum(matched, StockGroup.PRODUCT, NearExpiryLotResponse::getStockValue)
                )
                .build();

    }

    private long count(
            List<NearExpiryLotResponse> lots,
            StockGroup group
    ) {

        return lots.stream()
                .filter(lot -> lot.getGroup() == group)
                .count();

    }

    private BigDecimal sum(
            List<NearExpiryLotResponse> lots,
            StockGroup group,
            java.util.function.Function<NearExpiryLotResponse, BigDecimal> extractor
    ) {

        return lots.stream()
                .filter(lot -> lot.getGroup() == group)
                .map(extractor)
                .map(this::orZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    private BigDecimal orZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;

    }

    private String plain(BigDecimal value) {

        return orZero(value).stripTrailingZeros().toPlainString();

    }

}
