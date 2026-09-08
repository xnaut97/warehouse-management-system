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
import com.github.xnaut97.wms.repository.product.ProductIssueRepository;
import com.github.xnaut97.wms.repository.product.ProductReceiptRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.entity.product.ProductIssue;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.entity.product.ProductReceipt;
import com.github.xnaut97.wms.entity.product.ProductReceiptItem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {


    private static final int QUANTITY_SCALE = 0;

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

    private final ProductReceiptRepository productReceiptRepository;

    private final ProductIssueRepository productIssueRepository;

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

                receiptRepository.count()
                        + productReceiptRepository.count(),

                issueRepository.count()
                        + productIssueRepository.count(),

                getProductInventoryQuantity()

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

        BigDecimal stockIn = scaledQuantity(
                receiptItemRepository.getTotalQuantityByReceiptStatus(
                        ReceiptStatus.CONFIRMED
                )
        );

        BigDecimal stockOut = scaledQuantity(
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
                        scaledQuantity(stockIn.subtract(stockOut))
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
                        productIssueItemRepository.findSlowMovingProducts(
                                IssueStatus.CONFIRMED,
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

        List<RecentCandidate> candidates = new ArrayList<>();

        // 1. Goods Issues (Xuất kho NVL)
        List<GoodsIssue> goodsIssues = issueRepository.findTop10ByOrderByCreatedAtDesc();
        for (GoodsIssue gi : goodsIssues) {
            List<GoodsIssueItem> items = issueItemRepository.findByIssueId(gi.getId());
            String itemCode = items.isEmpty() ? "N/A" : items.get(0).getMaterial().getCode();
            BigDecimal totalQty = items.stream()
                    .map(i -> getOrZero(i.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            candidates.add(new RecentCandidate(
                    gi.getCreatedAt(),
                    gi.getIssueNo(),
                    RecentTransactionResponse.builder()
                            .id(gi.getId())
                            .time(gi.getCreatedAt() != null ? gi.getCreatedAt().format(formatter) : "")
                            .voucherNo(gi.getIssueNo())
                            .itemCode(itemCode)
                            .transactionType("ISSUE")
                            .itemCategory("Nguyên vật liệu")
                            .quantity(scaledQuantity(totalQty))
                            .status(gi.getStatus() != null ? gi.getStatus().name() : "PENDING")
                            .build()
            ));
        }

        // 2. Goods Receipts (Nhập kho NVL)
        List<GoodsReceipt> goodsReceipts = receiptRepository.findTop10ByOrderByCreatedAtDesc();
        for (GoodsReceipt gr : goodsReceipts) {
            List<GoodsReceiptItem> items = gr.getItems() != null ? gr.getItems() : List.of();
            String itemCode = items.isEmpty() ? "N/A" : items.get(0).getMaterial().getCode();
            BigDecimal totalQty = items.stream()
                    .map(i -> getOrZero(i.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            candidates.add(new RecentCandidate(
                    gr.getCreatedAt(),
                    gr.getReceiptNo(),
                    RecentTransactionResponse.builder()
                            .id(gr.getId())
                            .time(gr.getCreatedAt() != null ? gr.getCreatedAt().format(formatter) : "")
                            .voucherNo(gr.getReceiptNo())
                            .itemCode(itemCode)
                            .transactionType("RECEIPT")
                            .itemCategory("Nguyên vật liệu")
                            .quantity(scaledQuantity(totalQty))
                            .status(gr.getStatus() != null ? gr.getStatus().name() : "DRAFT")
                            .build()
            ));
        }

        // 3. Product Issues (Xuất kho Thành phẩm)
        List<ProductIssue> productIssues = productIssueRepository.findTop10ByOrderByCreatedAtDesc();
        for (ProductIssue pi : productIssues) {
            List<ProductIssueItem> items = pi.getItems() != null ? pi.getItems() : List.of();
            String itemCode = items.isEmpty() ? "N/A" : items.get(0).getProduct().getCode();
            BigDecimal totalQty = items.stream()
                    .map(i -> getOrZero(i.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            candidates.add(new RecentCandidate(
                    pi.getCreatedAt(),
                    pi.getIssueNo(),
                    RecentTransactionResponse.builder()
                            .id(pi.getId())
                            .time(pi.getCreatedAt() != null ? pi.getCreatedAt().format(formatter) : "")
                            .voucherNo(pi.getIssueNo())
                            .itemCode(itemCode)
                            .transactionType("ISSUE")
                            .itemCategory("Thành phẩm")
                            .quantity(scaledQuantity(totalQty))
                            .status(pi.getStatus() != null ? pi.getStatus().name() : "PENDING")
                            .build()
            ));
        }

        // 4. Product Receipts (Nhập kho Thành phẩm)
        List<ProductReceipt> productReceipts = productReceiptRepository.findTop10ByOrderByCreatedAtDesc();
        for (ProductReceipt pr : productReceipts) {
            List<ProductReceiptItem> items = pr.getItems() != null ? pr.getItems() : List.of();
            String itemCode = items.isEmpty() ? "N/A" : items.get(0).getProduct().getCode();
            BigDecimal totalQty = items.stream()
                    .map(i -> getOrZero(i.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            candidates.add(new RecentCandidate(
                    pr.getCreatedAt(),
                    pr.getReceiptNo(),
                    RecentTransactionResponse.builder()
                            .id(pr.getId())
                            .time(pr.getCreatedAt() != null ? pr.getCreatedAt().format(formatter) : "")
                            .voucherNo(pr.getReceiptNo())
                            .itemCode(itemCode)
                            .transactionType("RECEIPT")
                            .itemCategory("Thành phẩm")
                            .quantity(scaledQuantity(totalQty))
                            .status(pr.getStatus() != null ? pr.getStatus().name() : "DRAFT")
                            .build()
            ));
        }

        // 5. Inventory Transactions (Stock card / adjustments)
        List<InventoryTransaction> transactions = transactionRepository.findRecentTransactions(PageRequest.of(0, 10));
        for (InventoryTransaction t : transactions) {
            candidates.add(new RecentCandidate(
                    t.getCreatedAt(),
                    t.getReferenceNo(),
                    RecentTransactionResponse.builder()
                            .id(t.getId())
                            .time(t.getCreatedAt() != null ? t.getCreatedAt().format(formatter) : "")
                            .voucherNo(t.getReferenceNo())
                            .itemCode(t.getMaterial() != null ? t.getMaterial().getCode() : "N/A")
                            .transactionType(t.getType() == InventoryTransactionType.IN ? "RECEIPT" : "ISSUE")
                            .itemCategory("Nguyên vật liệu")
                            .quantity(scaledQuantity(t.getQuantity()))
                            .status("COMPLETED")
                            .build()
            ));
        }

        // Deduplicate by voucherNo, prioritizing document records over raw transaction rows
        Map<String, RecentCandidate> uniqueMap = new LinkedHashMap<>();
        for (RecentCandidate c : candidates) {
            if (c.voucherNo() != null && !c.voucherNo().isEmpty()) {
                if (!uniqueMap.containsKey(c.voucherNo())) {
                    uniqueMap.put(c.voucherNo(), c);
                } else {
                    RecentCandidate existing = uniqueMap.get(c.voucherNo());
                    if ("COMPLETED".equals(existing.response().getStatus()) && !"COMPLETED".equals(c.response().getStatus())) {
                        uniqueMap.put(c.voucherNo(), c);
                    }
                }
            } else {
                uniqueMap.put(java.util.UUID.randomUUID().toString(), c);
            }
        }

        return uniqueMap.values().stream()
                .filter(c -> c.createdAt() != null)
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
                .limit(10)
                .map(RecentCandidate::response)
                .toList();

    }

    private record RecentCandidate(
            LocalDateTime createdAt,
            String voucherNo,
            RecentTransactionResponse response
    ) {}

    private BigDecimal scaledQuantity(BigDecimal value) {

        return getOrZero(value).setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);

    }

    private BigDecimal getOrZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;

    }

    private BigDecimal getTotalInventoryQuantity() {

        BigDecimal total =
                materialInventoryRepository.getTotalQuantity();

        return scaledQuantity(total);

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

        return scaledQuantity(
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
