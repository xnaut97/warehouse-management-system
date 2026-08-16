package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse;
import com.github.xnaut97.wms.dto.report.operation.DocumentVolumeMonthlyResponse;
import com.github.xnaut97.wms.dto.report.operation.DocumentVolumeResponse;
import com.github.xnaut97.wms.dto.report.operation.MaterialConsumptionComparisonResponse;
import com.github.xnaut97.wms.dto.report.operation.MaterialQuantityResponse;
import com.github.xnaut97.wms.dto.report.operation.MaterialWasteRateResponse;
import com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationReportResponse;
import com.github.xnaut97.wms.dto.report.operation.StockSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.operation.StockSummaryRowResponse;
import com.github.xnaut97.wms.dto.report.operation.WeekdayFrequencyResponse;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.enums.DocumentType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.report.OperationReportRepository;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class OperationReportService {

    private static final int DEFAULT_PERIOD_MONTHS = 12;

    private static final int SCALE = 2;

    private final OperationReportRepository repository;

    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public OperationReportResponse getReport(
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
                        .minusMonths(DEFAULT_PERIOD_MONTHS - 1L)
                        .atDay(1);

        Map<Long, MaterialQuantityResponse> actual =
                indexByMaterial(repository.getActualMaterialConsumption(
                        IssueStatus.CONFIRMED,
                        resolvedFromDate,
                        resolvedToDate
                ));

        Map<Long, MaterialQuantityResponse> standard =
                indexByMaterial(repository.getStandardMaterialConsumption(
                        ReceiptStatus.CONFIRMED,
                        resolvedFromDate,
                        resolvedToDate
                ));

        return OperationReportResponse.builder()

                .fromDate(resolvedFromDate)

                .toDate(resolvedToDate)

                .wasteRate(
                        buildWasteRate(actual, standard)
                )

                .documentVolume(
                        buildDocumentVolume(resolvedFromDate, resolvedToDate)
                )

                .materialComparisons(
                        buildComparisons(actual, standard)
                )

                .weekdayFrequency(
                        buildWeekdayFrequency(resolvedFromDate, resolvedToDate)
                )

                .build();

    }

    @Transactional(readOnly = true)
    public StockSummaryReportResponse getStockSummary(
            StockGroup stockGroup,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        StockGroup resolvedGroup =
                stockGroup != null
                        ? stockGroup
                        : StockGroup.MATERIAL;

        LocalDate resolvedToDate =
                toDate != null
                        ? toDate
                        : LocalDate.now();

        LocalDate resolvedFromDate =
                fromDate != null && !fromDate.isAfter(resolvedToDate)
                        ? fromDate
                        : YearMonth.from(resolvedToDate).atDay(1);

        boolean material = resolvedGroup == StockGroup.MATERIAL;

        Warehouse warehouse = warehouseRepository.findByCode(
                material
                        ? WarehouseService.MATERIAL_WAREHOUSE_CODE
                        : WarehouseService.PRODUCT_WAREHOUSE_CODE
        ).orElseThrow(() -> new BusinessException("Không tìm thấy kho"));

        Long warehouseId = warehouse.getId();

        List<OperationQuantityResponse> currentStock =
                material
                        ? repository.getMaterialCurrentStock(warehouseId)
                        : repository.getProductCurrentStock(warehouseId);

        List<OperationQuantityResponse> receipts =
                material
                        ? repository.getMaterialReceiptQuantities(
                        ReceiptStatus.CONFIRMED,
                        warehouseId,
                        resolvedFromDate,
                        resolvedToDate
                )
                        : repository.getProductReceiptQuantities(
                        ReceiptStatus.CONFIRMED,
                        warehouseId,
                        resolvedFromDate,
                        resolvedToDate
                );

        List<OperationQuantityResponse> issues =
                material
                        ? repository.getMaterialIssueQuantities(
                        IssueStatus.CONFIRMED,
                        warehouseId,
                        resolvedFromDate,
                        resolvedToDate
                )
                        : repository.getProductIssueQuantities(
                        IssueStatus.CONFIRMED,
                        warehouseId,
                        resolvedFromDate,
                        resolvedToDate
                );

        List<OperationQuantityResponse> receiptsAfter =
                material
                        ? repository.getMaterialReceiptQuantitiesAfter(
                        ReceiptStatus.CONFIRMED,
                        warehouseId,
                        resolvedToDate
                )
                        : repository.getProductReceiptQuantitiesAfter(
                        ReceiptStatus.CONFIRMED,
                        warehouseId,
                        resolvedToDate
                );

        List<OperationQuantityResponse> issuesAfter =
                material
                        ? repository.getMaterialIssueQuantitiesAfter(
                        IssueStatus.CONFIRMED,
                        warehouseId,
                        resolvedToDate
                )
                        : repository.getProductIssueQuantitiesAfter(
                        IssueStatus.CONFIRMED,
                        warehouseId,
                        resolvedToDate
                );

        Map<Long, List<OperationDocumentResponse>> documents =
                groupDocuments(
                        material,
                        warehouseId,
                        resolvedFromDate,
                        resolvedToDate
                );

        Map<Long, OperationQuantityResponse> identity = new LinkedHashMap<>();

        indexInto(identity, currentStock);
        indexInto(identity, receipts);
        indexInto(identity, issues);

        Map<Long, BigDecimal> currentByItem = quantityByItem(currentStock);
        Map<Long, BigDecimal> receiptByItem = quantityByItem(receipts);
        Map<Long, BigDecimal> issueByItem = quantityByItem(issues);
        Map<Long, BigDecimal> receiptAfterByItem = quantityByItem(receiptsAfter);
        Map<Long, BigDecimal> issueAfterByItem = quantityByItem(issuesAfter);

        List<StockSummaryRowResponse> rows = new ArrayList<>();

        BigDecimal totalOpening = BigDecimal.ZERO;
        BigDecimal totalReceipt = BigDecimal.ZERO;
        BigDecimal totalIssue = BigDecimal.ZERO;
        BigDecimal totalClosing = BigDecimal.ZERO;

        for (Map.Entry<Long, OperationQuantityResponse> entry
                : identity.entrySet()) {

            Long itemId = entry.getKey();

            BigDecimal receiptQuantity = valueOf(receiptByItem, itemId);

            BigDecimal issueQuantity = valueOf(issueByItem, itemId);

            BigDecimal closingQuantity = valueOf(currentByItem, itemId)
                    .subtract(valueOf(receiptAfterByItem, itemId))
                    .add(valueOf(issueAfterByItem, itemId));

            BigDecimal openingQuantity = closingQuantity
                    .subtract(receiptQuantity)
                    .add(issueQuantity);

            totalOpening = totalOpening.add(openingQuantity);
            totalReceipt = totalReceipt.add(receiptQuantity);
            totalIssue = totalIssue.add(issueQuantity);
            totalClosing = totalClosing.add(closingQuantity);

            rows.add(StockSummaryRowResponse.builder()
                    .itemId(itemId)
                    .code(entry.getValue().getCode())
                    .name(entry.getValue().getName())
                    .unit(entry.getValue().getUnit())
                    .openingQuantity(scaled(openingQuantity))
                    .receiptQuantity(scaled(receiptQuantity))
                    .issueQuantity(scaled(issueQuantity))
                    .closingQuantity(scaled(closingQuantity))
                    .documents(
                            documents.getOrDefault(itemId, List.of())
                    )
                    .build());

        }

        rows.sort(Comparator.comparing(
                StockSummaryRowResponse::getCode,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return StockSummaryReportResponse.builder()
                .fromDate(resolvedFromDate)
                .toDate(resolvedToDate)
                .stockGroup(resolvedGroup)
                .warehouseId(warehouseId)
                .warehouseCode(warehouse.getCode())
                .warehouseName(warehouse.getName())
                .totalOpeningQuantity(scaled(totalOpening))
                .totalReceiptQuantity(scaled(totalReceipt))
                .totalIssueQuantity(scaled(totalIssue))
                .totalClosingQuantity(scaled(totalClosing))
                .items(rows)
                .build();

    }

    private Map<Long, List<OperationDocumentResponse>> groupDocuments(
            boolean material,
            Long warehouseId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<OperationDocumentResponse> documents = new ArrayList<>();

        if (material) {

            documents.addAll(stamp(
                    repository.getMaterialReceiptDocuments(
                            ReceiptStatus.CONFIRMED,
                            warehouseId,
                            fromDate,
                            toDate
                    ),
                    DocumentType.GOODS_RECEIPT
            ));

            documents.addAll(stamp(
                    repository.getMaterialIssueDocuments(
                            IssueStatus.CONFIRMED,
                            warehouseId,
                            fromDate,
                            toDate
                    ),
                    DocumentType.GOODS_ISSUE
            ));

        } else {

            documents.addAll(stamp(
                    repository.getProductReceiptDocuments(
                            ReceiptStatus.CONFIRMED,
                            warehouseId,
                            fromDate,
                            toDate
                    ),
                    DocumentType.PRODUCT_RECEIPT
            ));

            documents.addAll(stamp(
                    repository.getProductIssueDocuments(
                            IssueStatus.CONFIRMED,
                            warehouseId,
                            fromDate,
                            toDate
                    ),
                    DocumentType.PRODUCT_ISSUE
            ));

        }

        documents.sort(
                Comparator.comparing(
                                OperationDocumentResponse::getDocumentDate,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                OperationDocumentResponse::getDocumentNo,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
        );

        Map<Long, List<OperationDocumentResponse>> result =
                new LinkedHashMap<>();

        documents.forEach(document ->
                result.computeIfAbsent(
                        document.getItemId(),
                        key -> new ArrayList<>()
                ).add(document)
        );

        return result;

    }

    private List<OperationDocumentResponse> stamp(
            List<OperationDocumentResponse> documents,
            DocumentType documentType
    ) {

        documents.forEach(document ->
                document.setDocumentType(documentType)
        );

        return documents;

    }

    private void indexInto(
            Map<Long, OperationQuantityResponse> target,
            List<OperationQuantityResponse> rows
    ) {

        rows.forEach(row ->
                target.putIfAbsent(row.getItemId(), row)
        );

    }

    private Map<Long, BigDecimal> quantityByItem(
            List<OperationQuantityResponse> rows
    ) {

        Map<Long, BigDecimal> result = new LinkedHashMap<>();

        rows.forEach(row ->
                result.put(row.getItemId(), orZero(row.getQuantity()))
        );

        return result;

    }

    private BigDecimal valueOf(
            Map<Long, BigDecimal> source,
            Long itemId
    ) {

        return orZero(source.get(itemId));

    }

    private List<MaterialConsumptionComparisonResponse> buildComparisons(
            Map<Long, MaterialQuantityResponse> actual,
            Map<Long, MaterialQuantityResponse> standard
    ) {

        Map<Long, MaterialQuantityResponse> reference =
                new LinkedHashMap<>(standard);

        reference.putAll(actual);

        List<MaterialConsumptionComparisonResponse> result = new ArrayList<>();

        reference.forEach((materialId, material) -> {

            BigDecimal actualQuantity = quantityOf(actual, materialId);

            BigDecimal standardQuantity = quantityOf(standard, materialId);

            result.add(MaterialConsumptionComparisonResponse.builder()
                    .materialId(materialId)
                    .materialCode(material.getMaterialCode())
                    .materialName(material.getMaterialName())
                    .unit(material.getUnit())
                    .actualQuantity(scaled(actualQuantity))
                    .standardQuantity(scaled(standardQuantity))
                    .varianceQuantity(
                            scaled(actualQuantity.subtract(standardQuantity))
                    )
                    .wasteRatePercent(
                            wasteRate(actualQuantity, standardQuantity)
                    )
                    .build());

        });

        result.sort(Comparator.comparing(
                MaterialConsumptionComparisonResponse::getMaterialCode,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return result;

    }

    private MaterialWasteRateResponse buildWasteRate(
            Map<Long, MaterialQuantityResponse> actual,
            Map<Long, MaterialQuantityResponse> standard
    ) {

        BigDecimal totalActualQuantity = BigDecimal.ZERO;

        BigDecimal totalStandardQuantity = BigDecimal.ZERO;

        BigDecimal totalActualValue = BigDecimal.ZERO;

        BigDecimal totalStandardValue = BigDecimal.ZERO;

        for (Map.Entry<Long, MaterialQuantityResponse> entry
                : standard.entrySet()) {

            BigDecimal standardQuantity = orZero(entry.getValue().getQuantity());

            BigDecimal actualQuantity = quantityOf(actual, entry.getKey());

            BigDecimal unitPrice = orZero(entry.getValue().getUnitPrice());

            totalStandardQuantity = totalStandardQuantity.add(standardQuantity);

            totalActualQuantity = totalActualQuantity.add(actualQuantity);

            totalStandardValue = totalStandardValue.add(
                    standardQuantity.multiply(unitPrice)
            );

            totalActualValue = totalActualValue.add(
                    actualQuantity.multiply(unitPrice)
            );

        }

        if (totalStandardQuantity.compareTo(BigDecimal.ZERO) == 0) {

            return MaterialWasteRateResponse.builder()
                    .available(false)
                    .unavailableReason("NO_BOM_STANDARD_IN_PERIOD")
                    .totalActualQuantity(scaled(totalActualQuantity))
                    .totalStandardQuantity(BigDecimal.ZERO)
                    .totalActualValue(scaled(totalActualValue))
                    .totalStandardValue(BigDecimal.ZERO)
                    .comparedMaterials(standard.size())
                    .build();

        }

        return MaterialWasteRateResponse.builder()
                .available(true)
                .wasteRatePercent(
                        wasteRate(totalActualQuantity, totalStandardQuantity)
                )
                .wasteRateByValuePercent(
                        wasteRate(totalActualValue, totalStandardValue)
                )
                .totalActualQuantity(scaled(totalActualQuantity))
                .totalStandardQuantity(scaled(totalStandardQuantity))
                .totalActualValue(scaled(totalActualValue))
                .totalStandardValue(scaled(totalStandardValue))
                .comparedMaterials(standard.size())
                .build();

    }

    private DocumentVolumeResponse buildDocumentVolume(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        long materialReceipts = repository.countMaterialReceipts(
                ReceiptStatus.CONFIRMED,
                fromDate,
                toDate
        );

        long productReceipts = repository.countProductReceipts(
                ReceiptStatus.CONFIRMED,
                fromDate,
                toDate
        );

        long materialIssues = repository.countMaterialIssues(
                IssueStatus.CONFIRMED,
                fromDate,
                toDate
        );

        long productIssues = repository.countProductIssues(
                IssueStatus.CONFIRMED,
                fromDate,
                toDate
        );

        Map<YearMonth, long[]> monthly = new TreeMap<>();

        accumulateMonthly(
                monthly,
                repository.getMonthlyMaterialReceiptCount(
                        ReceiptStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                0
        );

        accumulateMonthly(
                monthly,
                repository.getMonthlyProductReceiptCount(
                        ReceiptStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                0
        );

        accumulateMonthly(
                monthly,
                repository.getMonthlyMaterialIssueCount(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                1
        );

        accumulateMonthly(
                monthly,
                repository.getMonthlyProductIssueCount(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                1
        );

        List<DocumentVolumeMonthlyResponse> monthlySeries =
                buildMonthlySeries(fromDate, toDate, monthly);

        return DocumentVolumeResponse.builder()
                .materialReceipts(materialReceipts)
                .productReceipts(productReceipts)
                .materialIssues(materialIssues)
                .productIssues(productIssues)
                .totalReceipts(materialReceipts + productReceipts)
                .totalIssues(materialIssues + productIssues)
                .totalDocuments(
                        materialReceipts
                                + productReceipts
                                + materialIssues
                                + productIssues
                )
                .monthly(monthlySeries)
                .build();

    }

    private List<DocumentVolumeMonthlyResponse> buildMonthlySeries(
            LocalDate fromDate,
            LocalDate toDate,
            Map<YearMonth, long[]> monthly
    ) {

        YearMonth startMonth = YearMonth.from(fromDate);

        YearMonth endMonth = YearMonth.from(toDate);

        long months = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;

        List<DocumentVolumeMonthlyResponse> series = new ArrayList<>();

        for (long offset = 0; offset < months; offset++) {

            YearMonth month = startMonth.plusMonths(offset);

            long[] counts = monthly.getOrDefault(month, new long[]{0, 0});

            series.add(DocumentVolumeMonthlyResponse.builder()
                    .month(month.toString())
                    .receiptCount(counts[0])
                    .issueCount(counts[1])
                    .totalCount(counts[0] + counts[1])
                    .build());

        }

        return series;

    }

    private void accumulateMonthly(
            Map<YearMonth, long[]> target,
            List<MonthlyCountResponse> rows,
            int slot
    ) {

        rows.forEach(row -> {

            long[] counts = target.computeIfAbsent(
                    YearMonth.of(row.getYear(), row.getMonth()),
                    key -> new long[]{0, 0}
            );

            counts[slot] += row.getTotal() == null
                    ? 0
                    : row.getTotal();

        });

    }

    private List<WeekdayFrequencyResponse> buildWeekdayFrequency(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        long[][] counts = new long[7][2];

        accumulateWeekday(
                counts,
                repository.getMaterialReceiptCountByDate(
                        ReceiptStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                0
        );

        accumulateWeekday(
                counts,
                repository.getProductReceiptCountByDate(
                        ReceiptStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                0
        );

        accumulateWeekday(
                counts,
                repository.getMaterialIssueCountByDate(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                1
        );

        accumulateWeekday(
                counts,
                repository.getProductIssueCountByDate(
                        IssueStatus.CONFIRMED,
                        fromDate,
                        toDate
                ),
                1
        );

        List<WeekdayFrequencyResponse> result = new ArrayList<>();

        for (DayOfWeek day : DayOfWeek.values()) {

            long[] dayCounts = counts[day.getValue() - 1];

            result.add(WeekdayFrequencyResponse.builder()
                    .dayOfWeek(day.getValue())
                    .dayCode(day.name())
                    .receiptCount(dayCounts[0])
                    .issueCount(dayCounts[1])
                    .totalCount(dayCounts[0] + dayCounts[1])
                    .build());

        }

        return result;

    }

    private void accumulateWeekday(
            long[][] target,
            List<DocumentDateCountResponse> rows,
            int slot
    ) {

        rows.forEach(row -> {

            if (row.getDocumentDate() == null) {
                return;
            }

            int index = row.getDocumentDate()
                    .getDayOfWeek()
                    .getValue() - 1;

            target[index][slot] += row.getTotal() == null
                    ? 0
                    : row.getTotal();

        });

    }

    private Map<Long, MaterialQuantityResponse> indexByMaterial(
            List<MaterialQuantityResponse> rows
    ) {

        Map<Long, MaterialQuantityResponse> result = new LinkedHashMap<>();

        rows.forEach(row -> result.put(row.getMaterialId(), row));

        return result;

    }

    private BigDecimal quantityOf(
            Map<Long, MaterialQuantityResponse> source,
            Long materialId
    ) {

        MaterialQuantityResponse row = source.get(materialId);

        return row == null
                ? BigDecimal.ZERO
                : orZero(row.getQuantity());

    }

    private BigDecimal wasteRate(
            BigDecimal actual,
            BigDecimal standard
    ) {

        if (standard == null || standard.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return actual.subtract(standard)
                .multiply(BigDecimal.valueOf(100))
                .divide(standard, SCALE, RoundingMode.HALF_UP);

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
