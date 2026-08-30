package com.github.xnaut97.wms.service;

import com.github.xnaut97.wms.dto.dashboard.*;
import com.github.xnaut97.wms.repository.*;
import com.github.xnaut97.wms.repository.goods.GoodsIssueRepository;
import com.github.xnaut97.wms.repository.goods.GoodsIssueItemRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptRepository;
import com.github.xnaut97.wms.repository.goods.GoodsReceiptItemRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryTransactionRepository;
import com.github.xnaut97.wms.repository.product.ProductIssueItemRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptItemRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
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

    private final MaterialRepository materialRepository;

    private final ProductRepository productRepository;

    private final MaterialInventoryRepository materialInventoryRepository;

    private final ProductInventoryRepository productInventoryRepository;

    private final GoodsReceiptRepository receiptRepository;

    private final GoodsIssueRepository issueRepository;

    private final GoodsReceiptItemRepository receiptItemRepository;

    private final GoodsIssueItemRepository issueItemRepository;

    private final ProductReceiptItemRepository productReceiptItemRepository;

    private final ProductIssueItemRepository productIssueItemRepository;

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
                        materialInventoryRepository.count()
                )

                .lowStockItems(
                        materialInventoryRepository.countLowStock()
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
                                getTotalInventoryValue()
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

                productRepository.count(),

                getOrZero(
                        getTotalInventoryValue()
                ),

                getOrZero(
                        receiptItemRepository.getTotalQuantity()
                ).add(
                        getOrZero(
                                productReceiptItemRepository.getTotalQuantity()
                        )
                ),

                getOrZero(
                        issueItemRepository.getTotalQuantity()
                ).add(
                        getOrZero(
                                productIssueItemRepository.getTotalQuantity()
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

                .materialInventory(
                        getTotalInventoryQuantity()
                )

                .productInventory(
                        getProductInventoryQuantity()
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
                        getTotalInventoryValue()
                )

                .materialInventoryValue(
                        getMaterialInventoryValue()
                )

                .productInventoryValue(
                        getProductInventoryValue()
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
                        materialInventoryRepository.findLowStockMaterials()
                )

                .replenishmentRecommendations(
                        materialInventoryRepository.findReplenishmentRecommendations()
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

        return materialInventoryRepository.findLowStockItems()

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

        return materialInventoryRepository.findLowStockItems()

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

        return getInventoryTrendForLastTwelveMonths()

                .stream()

                .map(row ->

                        new InventoryTrendResponse(

                                row.getMonth(),

                                row.getStockIn(),

                                row.getStockOut()

                        )

                )

                .toList();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public OperationAlertResponse operationAlerts() {

        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(60);

        List<NearExpirationAlertResponse> nearExpiration =
                receiptItemRepository.findNearExpirationItems(today, deadline)
                        .stream()
                        .map(item -> new NearExpirationAlertResponse(
                                item.getLotNumber() != null
                                        ? item.getLotNumber()
                                        : item.getMaterial().getCode(),
                                item.getMaterial().getName()
                                        + (item.getLotNumber() != null
                                        ? " - " + item.getLotNumber()
                                        : ""),
                                item.getExpirationDate(),
                                java.time.temporal.ChronoUnit.DAYS.between(
                                        today,
                                        item.getExpirationDate()
                                )
                        ))
                        .toList();

        return OperationAlertResponse.builder()
                .belowMin(materialInventoryRepository.findBelowMinAlerts())
                .aboveMax(materialInventoryRepository.findAboveMaxAlerts())
                .nearExpiration(nearExpiration)
                .build();

    }

    @Audit(
            action = AuditAction.READ,
            entity = "Dashboard"
    )
    @Transactional
    public List<RecentTransactionResponse> recentTransactions() {

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return transactionRepository
                .findRecentTransactions(PageRequest.of(0, 10))
                .stream()
                .map(t -> RecentTransactionResponse.builder()
                        .id(t.getId())
                        .time(t.getCreatedAt().format(formatter))
                        .voucherNo(t.getReferenceNo())
                        .itemCode(t.getMaterial().getCode())
                        .transactionType(
                                t.getType() == InventoryTransactionType.IN
                                        ? "RECEIPT"
                                        : "ISSUE"
                        )
                        .itemCategory("Nguyên vật liệu")
                        .quantity(t.getQuantity())
                        .status("COMPLETED")
                        .build()
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
                materialInventoryRepository.getTotalQuantity();

        return total == null
                ? BigDecimal.ZERO
                : total;

    }

    private BigDecimal getMaterialInventoryValue() {

        return getOrZero(
                materialInventoryRepository.getTotalInventoryValue()
        );

    }

    private BigDecimal getProductInventoryValue() {

        return getOrZero(
                productInventoryRepository.getTotalInventoryValue()
        );

    }

    private BigDecimal getTotalInventoryValue() {

        return getMaterialInventoryValue()
                .add(getProductInventoryValue());

    }

    private BigDecimal getProductInventoryQuantity() {

        return getOrZero(
                productInventoryRepository.getTotalQuantity()
        );

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
