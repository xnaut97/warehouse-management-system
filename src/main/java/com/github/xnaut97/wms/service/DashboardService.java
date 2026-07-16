package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.dto.dashboard.*;
import com.github.xnaut97.wms.repository.*;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingItemRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.annotation.Audit;
import com.github.xnaut97.wms.enums.AuditAction;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WarehouseRepository warehouseRepository;

    private final SupplierRepository supplierRepository;

    private final CustomerRepository customerRepository;

    private final RawMaterialRepository materialRepository;

    private final FinishedProductRepository finishedProductRepository;

    private final InventoryRepository inventoryRepository;

    private final GoodsReceiptRepository receiptRepository;

    private final GoodsIssueRepository issueRepository;

    private final GoodsReceiptItemRepository receiptItemRepository;

    private final GoodsIssueItemRepository issueItemRepository;

    private final InventoryTransactionRepository transactionRepository;

    private final StocktakingRepository stocktakingRepository;

    private final StocktakingItemRepository stocktakingItemRepository;

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public DashboardSummaryResponse summary() {

        LocalDate firstDay =
                LocalDate.now().withDayOfMonth(1);

        LocalDate lastDay =
                firstDay.withDayOfMonth(
                        firstDay.lengthOfMonth()
                );

        return DashboardSummaryResponse.builder()

                .warehouses(
                        warehouseRepository.count()
                )

                .suppliers(
                        supplierRepository.count()
                )

                .customers(
                        customerRepository.count()
                )

                .materials(
                        materialRepository.count()
                )

                .inventoryRecords(
                        inventoryRepository.count()
                )

                .lowStockItems(
                        inventoryRepository.countLowStock()
                )

                .receiptsThisMonth(
                        receiptRepository.countByReceiptDateBetween(
                                firstDay,
                                lastDay
                        )
                )

                .issuesThisMonth(
                        issueRepository.countByIssueDateBetween(
                                firstDay,
                                lastDay
                        )
                )

                .totalInventoryQuantity(
                        getTotalInventoryQuantity()
                )

                .totalInventoryValue(
                        getOrZero(
                                inventoryRepository.getTotalInventoryValue()
                        )
                )

                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public DashboardOverviewResponse overview() {

        return new DashboardOverviewResponse(

                materialRepository.count(),

                finishedProductRepository.count(),

                getOrZero(
                        inventoryRepository.getTotalInventoryValue()
                ),

                getOrZero(
                        receiptItemRepository.getTotalQuantityByReceiptStatus(
                                ReceiptStatus.CONFIRMED
                        )
                ),

                getOrZero(
                        issueItemRepository.getTotalQuantityByIssueStatus(
                                IssueStatus.CONFIRMED
                        )
                ),

                getTotalInventoryQuantity()

        );

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<MonthlyStatisticResponse> monthlyReceipts() {

        return receiptRepository
                .monthlyReceiptStatistics()
                .stream()
                .map(row -> {

                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    Long total = (Long) row[2];

                    return new MonthlyStatisticResponse(
                            String.format("%04d-%02d", year, month),
                            total
                    );

                })
                .toList();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<MonthlyStatisticResponse> monthlyIssues() {

        return issueRepository
                .monthlyIssueStatistics()
                .stream()
                .map(row -> {

                    Integer year = (Integer) row[0];
                    Integer month = (Integer) row[1];
                    Long total = (Long) row[2];

                    return new MonthlyStatisticResponse(
                            String.format("%04d-%02d", year, month),
                            total
                    );

                })
                .toList();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public InventoryAnalysisResponse inventoryAnalysis() {

        BigDecimal stockIn = getOrZero(
                receiptItemRepository.getTotalQuantityByReceiptStatus(
                        ReceiptStatus.CONFIRMED
                )
        );

        BigDecimal stockOut = getOrZero(
                issueItemRepository.getTotalQuantityByIssueStatus(
                        IssueStatus.CONFIRMED
                )
        );

        return InventoryAnalysisResponse.builder()

                .rawMaterialInventory(
                        materialRepository.count()
                )

                .finishedProductInventory(
                        finishedProductRepository.count()
                )

                .stockIn(
                        stockIn
                )

                .stockOut(
                        stockOut
                )

                .stockBalance(
                        stockIn.subtract(stockOut)
                )

                .inventoryValue(
                        getOrZero(
                                inventoryRepository.getTotalInventoryValue()
                        )
                )

                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public InventoryVarianceResponse inventoryVariance() {

        return InventoryVarianceResponse.builder()

                .totalStocktakingRecords(
                        stocktakingRepository.count()
                )

                .totalVarianceQuantity(
                        getOrZero(
                                stocktakingItemRepository.getTotalVarianceQuantity()
                        )
                )

                .totalVarianceValue(
                        getOrZero(
                                stocktakingItemRepository.getTotalVarianceValue()
                        )
                )

                .topVarianceItems(
                        stocktakingItemRepository.findTopVarianceItems(
                                PageRequest.of(0, 10)
                        )
                )

                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public DecisionSupportResponse decisionSupport() {

        return DecisionSupportResponse.builder()

                .lowStockMaterials(
                        inventoryRepository.findLowStockMaterials()
                )

                .replenishmentRecommendations(
                        inventoryRepository.findReplenishmentRecommendations()
                )

                .slowMovingMaterials(
                        transactionRepository.findSlowMovingMaterials(
                                InventoryTransactionType.OUT,
                                LocalDateTime.now().minusDays(90)
                        )
                )

                .highVarianceMaterials(
                        stocktakingItemRepository.findHighVarianceMaterials(
                                PageRequest.of(0, 10)
                        )
                )

                .inventoryTrend(
                        getInventoryTrendForLastTwelveMonths()
                )

                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<LowStockAlertResponse> lowStockAlerts() {

        return inventoryRepository.findLowStockItems()

                .stream()

                .map(i ->

                        LowStockAlertResponse.builder()

                                .materialId(
                                        i.getMaterial().getId()
                                )

                                .materialCode(
                                        i.getMaterial().getCode()
                                )

                                .materialName(
                                        i.getMaterial().getName()
                                )

                                .warehouse(
                                        i.getWarehouse().getName()
                                )

                                .currentQuantity(
                                        i.getQuantity()
                                )

                                .minimumStock(
                                        i.getMaterial().getMinimumStock()
                                )

                                .build()

                )

                .toList();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<ReplenishmentRecommendationResponse> replenishmentRecommendations(){

        return inventoryRepository.findLowStockItems()

                .stream()

                .map(i -> {

                    BigDecimal recommendation =

                            i.getMaterial()

                                    .getMinimumStock()

                                    .subtract(i.getQuantity());

                    return ReplenishmentRecommendationResponse
                            .builder()

                            .materialId(
                                    i.getMaterial().getId()
                            )

                            .materialCode(
                                    i.getMaterial().getCode()
                            )

                            .materialName(
                                    i.getMaterial().getName()
                            )

                            .warehouse(
                                    i.getWarehouse().getName()
                            )

                            .currentQuantity(
                                    i.getQuantity()
                            )

                            .minimumStock(
                                    i.getMaterial().getMinimumStock()
                            )

                            .recommendedOrder(
                                    recommendation
                            )

                            .build();

                })

                .toList();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<InventoryTrendResponse> inventoryTrend() {

        return transactionRepository.inventoryTrend()

                .stream()

                .map(row ->

                        new InventoryTrendResponse(

                                String.format("%04d-%02d", row[0], row[1]),

                                (BigDecimal) row[2],

                                (BigDecimal) row[3]

                        )

                )

                .toList();

    }

    private BigDecimal getOrZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;

    }

    private BigDecimal getTotalInventoryQuantity() {

        BigDecimal total =
                inventoryRepository.getTotalQuantity();

        return total == null
                ? BigDecimal.ZERO
                : total;

    }

    private List<DashboardInventoryTrendResponse> getInventoryTrendForLastTwelveMonths() {

        YearMonth currentMonth = YearMonth.now();

        YearMonth startMonth = currentMonth.minusMonths(11);

        LocalDate fromDate = startMonth.atDay(1);

        Map<YearMonth, BigDecimal> stockInByMonth =
                toMonthlyQuantityMap(
                        receiptItemRepository.getMonthlyStockIn(
                                ReceiptStatus.CONFIRMED,
                                fromDate
                        )
                );

        Map<YearMonth, BigDecimal> stockOutByMonth =
                toMonthlyQuantityMap(
                        issueItemRepository.getMonthlyStockOut(
                                IssueStatus.CONFIRMED,
                                fromDate
                        )
                );

        return IntStream.rangeClosed(0, 11)

                .mapToObj(startMonth::plusMonths)

                .map(month -> {

                    BigDecimal stockIn =
                            stockInByMonth.getOrDefault(
                                    month,
                                    BigDecimal.ZERO
                            );

                    BigDecimal stockOut =
                            stockOutByMonth.getOrDefault(
                                    month,
                                    BigDecimal.ZERO
                            );

                    return new DashboardInventoryTrendResponse(

                            month.toString(),

                            stockIn,

                            stockOut,

                            stockIn.subtract(stockOut)

                    );

                })

                .toList();

    }

    private Map<YearMonth, BigDecimal> toMonthlyQuantityMap(

            List<DashboardMonthlyQuantityResponse> rows

    ) {

        Map<YearMonth, BigDecimal> result = new HashMap<>();

        rows.forEach(row ->

                result.put(

                        YearMonth.of(
                                row.getYear(),
                                row.getMonth()
                        ),

                        getOrZero(
                                row.getQuantity()
                        )

                )

        );

        return result;

    }

}
