package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.InventoryDetailResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryLotResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryResponse;
import com.github.xnaut97.wms.dto.inventory.InventorySummaryResponse;
import com.github.xnaut97.wms.dto.inventory.InventorySummaryRowResponse;
import com.github.xnaut97.wms.dto.inventory.LowStockResponse;
import com.github.xnaut97.wms.dto.report.operation.StockSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.operation.StockSummaryRowResponse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.entity.product.Product;
import com.github.xnaut97.wms.enums.ExpiryStatus;
import com.github.xnaut97.wms.enums.InventoryStatus;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StockStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.repository.product.ProductRepository;
import com.github.xnaut97.wms.service.alert.AlertService;
import com.github.xnaut97.wms.service.report.OperationReportService;
import com.github.xnaut97.wms.specification.InventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Comparator<String> TEXT =
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER);

    private static final int VALUE_SCALE = 2;

    private final MaterialInventoryRepository repository;

    private final ProductInventoryRepository productRepository;

    private final MaterialRepository materialRepository;

    private final ProductRepository productCatalogRepository;

    private final OperationReportService operationReportService;

    @Transactional
    public List<LowStockResponse> getLowStock() {

        return repository.findAll(
                        InventorySpecification.lowStock()
                ).stream()
                .map(this::mapLowStock)
                .toList();

    }

    @Transactional
    public Page<InventoryResponse> getAll(

            Long warehouseId,

            Long materialId,

            String keyword,

            Pageable pageable

    ) {

        List<InventoryResponse> items = new ArrayList<>(

                repository.findAll(

                        InventorySpecification.filter(

                                warehouseId,

                                materialId,

                                keyword

                        )

                ).stream()
                        .map(this::map)
                        .toList()

        );

        if (materialId == null) {

            items.addAll(
                    findProductInventories(
                            warehouseId,
                            keyword
                    )
            );

        }

        items.sort(
                comparator(pageable.getSort())
        );

        return paginate(items, pageable);

    }

    /**
     * Bảng tồn kho theo kỳ của kho nguyên vật liệu hoặc kho sản phẩm. Số liệu
     * tồn đầu, nhập, xuất, tồn cuối lấy nguyên từ báo cáo nhập xuất tồn hiện có
     * để hai màn hình luôn khớp nhau; phần bổ sung ở đây là giá bình quân, giá
     * trị vốn tồn, trạng thái định mức và danh sách lô của sản phẩm.
     */
    @Transactional(readOnly = true)
    public InventorySummaryResponse getSummary(
            StockGroup stockGroup,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        StockSummaryReportResponse summary =
                operationReportService.getStockSummary(
                        stockGroup,
                        fromDate,
                        toDate,
                        false
                );

        List<InventorySummaryRowResponse> items =
                summary.getStockGroup() == StockGroup.PRODUCT
                        ? productRows(summary)
                        : materialRows(summary);

        return InventorySummaryResponse.builder()
                .fromDate(summary.getFromDate())
                .toDate(summary.getToDate())
                .stockGroup(summary.getStockGroup())
                .warehouseId(summary.getWarehouseId())
                .warehouseCode(summary.getWarehouseCode())
                .warehouseName(summary.getWarehouseName())
                .totalOpeningQuantity(summary.getTotalOpeningQuantity())
                .totalReceiptQuantity(summary.getTotalReceiptQuantity())
                .totalIssueQuantity(summary.getTotalIssueQuantity())
                .totalClosingQuantity(summary.getTotalClosingQuantity())
                .totalInventoryValue(
                        scaled(
                                items.stream()
                                        .map(InventorySummaryRowResponse::getInventoryValue)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        )
                )
                .items(items)
                .build();

    }

    @Transactional
    public InventoryDetailResponse getDetail(
            Long id
    ) {

        MaterialInventory materialInventory = findInventoryById(id);

        return mapDetail(materialInventory);

    }

    public MaterialInventory findInventoryById(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy tồn kho."
                        ));

    }

    private List<InventorySummaryRowResponse> materialRows(
            StockSummaryReportResponse summary
    ) {

        Map<Long, Material> materials = new LinkedHashMap<>();

        materialRepository.findAllByEnabledTrue()
                .forEach(material -> materials.put(material.getId(), material));

        materialRepository.findAllById(itemIds(summary))
                .forEach(material -> materials.put(material.getId(), material));

        List<StockSummaryRowResponse> sourceRows = withCatalogItems(
                summary,
                materials.values().stream()
                        .map(material -> emptyRow(
                                material.getId(),
                                material.getCode(),
                                material.getName(),
                                material.getUnit()
                        ))
                        .toList()
        );

        List<InventorySummaryRowResponse> rows = new ArrayList<>();

        for (StockSummaryRowResponse row : sourceRows) {

            Material material = materials.get(row.getItemId());

            BigDecimal averagePrice = material == null
                    ? BigDecimal.ZERO
                    : orZero(material.getUnitPrice());

            BigDecimal minimumStock = material == null
                    ? BigDecimal.ZERO
                    : orZero(material.getMinimumStock());

            BigDecimal maximumStock = material == null
                    ? BigDecimal.ZERO
                    : orZero(material.getMaximumStock());

            rows.add(baseRow(row)
                    .averagePrice(scaled(averagePrice))
                    .inventoryValue(
                            scaled(orZero(row.getClosingQuantity())
                                    .multiply(averagePrice))
                    )
                    .minimumStock(minimumStock)
                    .maximumStock(maximumStock)
                    .thresholdStatus(
                            thresholdStatus(
                                    orZero(row.getClosingQuantity()),
                                    minimumStock,
                                    maximumStock
                            )
                    )
                    .build());

        }

        return rows;

    }

    private List<InventorySummaryRowResponse> productRows(
            StockSummaryReportResponse summary
    ) {

        Map<Long, Product> products = new LinkedHashMap<>();

        productCatalogRepository.findAllByEnabledTrue()
                .forEach(product -> products.put(product.getId(), product));

        productCatalogRepository.findAllById(itemIds(summary))
                .forEach(product -> products.put(product.getId(), product));

        LocalDate today = LocalDate.now();

        Map<Long, List<InventoryLotResponse>> lots =
                lotsByProduct(summary.getWarehouseId(), today);

        List<StockSummaryRowResponse> sourceRows = withCatalogItems(
                summary,
                products.values().stream()
                        .map(product -> emptyRow(
                                product.getId(),
                                product.getCode(),
                                product.getName(),
                                product.getUnit()
                        ))
                        .toList()
        );

        List<InventorySummaryRowResponse> rows = new ArrayList<>();

        for (StockSummaryRowResponse row : sourceRows) {

            Product product = products.get(row.getItemId());

            BigDecimal averagePrice = product == null
                    ? BigDecimal.ZERO
                    : orZero(product.getAveragePrice());

            List<InventoryLotResponse> productLots =
                    lots.getOrDefault(row.getItemId(), List.of());

            rows.add(baseRow(row)
                    .averagePrice(scaled(averagePrice))
                    .inventoryValue(
                            scaled(orZero(row.getClosingQuantity())
                                    .multiply(averagePrice))
                    )
                    .minimumStock(
                            product == null
                                    ? BigDecimal.ZERO
                                    : orZero(product.getMinimumStock())
                    )
                    .maximumStock(
                            product == null
                                    ? BigDecimal.ZERO
                                    : orZero(product.getMaximumStock())
                    )
                    .expiryStatus(primaryLotStatus(productLots))
                    .lots(productLots)
                    .build());

        }

        return rows;

    }

    /**
     * Lô còn tồn của kho sản phẩm, giữ nguyên thứ tự FEFO (hạn dùng tăng dần)
     * do repository trả về.
     */
    private Map<Long, List<InventoryLotResponse>> lotsByProduct(
            Long warehouseId,
            LocalDate today
    ) {

        Map<Long, List<InventoryLotResponse>> result =
                new LinkedHashMap<>();

        for (ProductInventory lot
                : productRepository.findAvailableLots(warehouseId)) {

            result.computeIfAbsent(
                    lot.getProduct().getId(),
                    key -> new ArrayList<>()
            ).add(mapLot(lot, today));

        }

        return result;

    }

    private InventoryLotResponse mapLot(
            ProductInventory lot,
            LocalDate today
    ) {

        Long daysToExpiry = lot.getExpirationDate() == null
                ? null
                : ChronoUnit.DAYS.between(
                        today,
                        lot.getExpirationDate()
                );

        return InventoryLotResponse.builder()
                .inventoryId(lot.getId())
                .lotNumber(lot.getLotNumber())
                .expirationDate(lot.getExpirationDate())
                .daysToExpiry(daysToExpiry)
                .quantity(lot.getQuantity())
                .status(expiryStatus(daysToExpiry))
                .build();

    }

    /**
     * Ngưỡng cảnh báo hạn dùng dùng chung với trung tâm cảnh báo.
     */
    private ExpiryStatus expiryStatus(Long daysToExpiry) {

        return daysToExpiry != null
                && daysToExpiry <= AlertService.NEAR_EXPIRY_CRITICAL_DAYS
                ? ExpiryStatus.FEFO
                : ExpiryStatus.SAFE;

    }

    /**
     * Lô hết hạn sớm nhất quyết định trạng thái của sản phẩm, nên dòng cha và
     * bảng lô không bao giờ mâu thuẫn nhau.
     */
    private ExpiryStatus primaryLotStatus(
            List<InventoryLotResponse> lots
    ) {

        return lots.stream()
                .map(InventoryLotResponse::getStatus)
                .filter(status -> status == ExpiryStatus.FEFO)
                .findFirst()
                .orElse(ExpiryStatus.SAFE);

    }

    private InventoryStatus thresholdStatus(
            BigDecimal quantity,
            BigDecimal minimumStock,
            BigDecimal maximumStock
    ) {

        if (quantity.compareTo(minimumStock) < 0) {
            return InventoryStatus.BELOW_MIN;
        }

        if (maximumStock.compareTo(BigDecimal.ZERO) > 0
                && quantity.compareTo(maximumStock) > 0) {
            return InventoryStatus.ABOVE_MAX;
        }

        return InventoryStatus.NORMAL;

    }

    private InventorySummaryRowResponse.InventorySummaryRowResponseBuilder baseRow(
            StockSummaryRowResponse row
    ) {

        return InventorySummaryRowResponse.builder()
                .itemId(row.getItemId())
                .code(row.getCode())
                .name(row.getName())
                .unit(row.getUnit())
                .openingQuantity(row.getOpeningQuantity())
                .receiptQuantity(row.getReceiptQuantity())
                .issueQuantity(row.getIssueQuantity())
                .closingQuantity(row.getClosingQuantity());

    }

    private List<StockSummaryRowResponse> withCatalogItems(
            StockSummaryReportResponse summary,
            List<StockSummaryRowResponse> catalogRows
    ) {

        Map<Long, StockSummaryRowResponse> merged = new LinkedHashMap<>();

        catalogRows.forEach(row -> merged.put(row.getItemId(), row));

        summary.getItems().forEach(row -> merged.put(row.getItemId(), row));

        List<StockSummaryRowResponse> rows = new ArrayList<>(merged.values());

        rows.sort(Comparator.comparing(
                StockSummaryRowResponse::getCode,
                Comparator.nullsLast(Comparator.naturalOrder())
        ));

        return rows;

    }

    private StockSummaryRowResponse emptyRow(
            Long itemId,
            String code,
            String name,
            String unit
    ) {

        BigDecimal zero = scaled(BigDecimal.ZERO);

        return StockSummaryRowResponse.builder()
                .itemId(itemId)
                .code(code)
                .name(name)
                .unit(unit)
                .openingQuantity(zero)
                .receiptQuantity(zero)
                .issueQuantity(zero)
                .closingQuantity(zero)
                .documents(List.of())
                .build();

    }

    private List<Long> itemIds(
            StockSummaryReportResponse summary
    ) {

        return summary.getItems().stream()
                .map(StockSummaryRowResponse::getItemId)
                .toList();

    }

    private BigDecimal scaled(BigDecimal value) {

        return orZero(value).setScale(VALUE_SCALE, RoundingMode.HALF_UP);

    }

    private BigDecimal orZero(BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;

    }

    private List<InventoryResponse> findProductInventories(
            Long warehouseId,
            String keyword
    ) {

        String search = keyword == null || keyword.isBlank()
                ? null
                : "%" + keyword.toLowerCase() + "%";

        List<ProductInventory> lots =
                productRepository.findAllForInventoryList(
                        warehouseId,
                        search
                );

        Map<String, InventoryResponse> merged =
                new LinkedHashMap<>();

        for (ProductInventory lot : lots) {

            String key = lot.getWarehouse().getId()
                    + "-"
                    + lot.getProduct().getId();

            InventoryResponse current = merged.get(key);

            BigDecimal quantity = current == null
                    ? lot.getQuantity()
                    : current.getQuantity().add(lot.getQuantity());

            merged.put(
                    key,
                    InventoryResponse.builder()
                            .itemGroup(StockGroup.PRODUCT)
                            .warehouseId(lot.getWarehouse().getId())
                            .warehouse(lot.getWarehouse().getName())
                            .productId(lot.getProduct().getId())
                            .code(lot.getProduct().getCode())
                            .name(lot.getProduct().getName())
                            .unit(lot.getProduct().getUnit())
                            .quantity(quantity)
                            .build()
            );

        }

        return new ArrayList<>(merged.values());

    }

    private Comparator<InventoryResponse> comparator(
            Sort sort
    ) {

        Comparator<InventoryResponse> result = null;

        for (Sort.Order order : sort) {

            Comparator<InventoryResponse> current =
                    comparatorFor(order.getProperty());

            if (current == null) {
                continue;
            }

            if (order.isDescending()) {
                current = current.reversed();
            }

            result = result == null
                    ? current
                    : result.thenComparing(current);

        }

        Comparator<InventoryResponse> fallback =
                Comparator.comparing(
                        InventoryResponse::getName,
                        TEXT
                );

        return result == null
                ? fallback
                : result.thenComparing(fallback);

    }

    private Comparator<InventoryResponse> comparatorFor(
            String property
    ) {

        return switch (property) {

            case "warehouse" -> Comparator.comparing(
                    InventoryResponse::getWarehouse,
                    TEXT
            );

            case "code" -> Comparator.comparing(
                    InventoryResponse::getCode,
                    TEXT
            );

            case "name" -> Comparator.comparing(
                    InventoryResponse::getName,
                    TEXT
            );

            case "unit" -> Comparator.comparing(
                    InventoryResponse::getUnit,
                    TEXT
            );

            case "itemGroup" -> Comparator.comparing(
                    inventory -> inventory.getItemGroup().name(),
                    TEXT
            );

            case "quantity" -> Comparator.comparing(
                    InventoryResponse::getQuantity,
                    Comparator.nullsLast(
                            Comparator.<BigDecimal>naturalOrder()
                    )
            );

            default -> null;

        };

    }

    private Page<InventoryResponse> paginate(
            List<InventoryResponse> items,
            Pageable pageable
    ) {

        if (pageable.isUnpaged()) {

            return new PageImpl<>(
                    items,
                    pageable,
                    items.size()
            );

        }

        int start = (int) Math.min(
                pageable.getOffset(),
                items.size()
        );

        int end = Math.min(
                start + pageable.getPageSize(),
                items.size()
        );

        return new PageImpl<>(
                items.subList(start, end),
                pageable,
                items.size()
        );

    }

    private InventoryResponse map(
            MaterialInventory materialInventory
    ) {

        return InventoryResponse.builder()
                .id(materialInventory.getId())
                .itemGroup(StockGroup.MATERIAL)
                .warehouseId(materialInventory.getWarehouse().getId())
                .warehouse(materialInventory.getWarehouse().getName())
                .materialId(materialInventory.getMaterial().getId())
                .code(materialInventory.getMaterial().getCode())
                .name(materialInventory.getMaterial().getName())
                .unit(materialInventory.getMaterial().getUnit())
                .quantity(materialInventory.getQuantity())
                .build();

    }

    private InventoryDetailResponse mapDetail(
            MaterialInventory materialInventory
    ) {

        return InventoryDetailResponse.builder()
                .id(materialInventory.getId())
                .warehouseId(materialInventory.getWarehouse().getId())
                .warehouse(materialInventory.getWarehouse().getName())
                .materialId(materialInventory.getMaterial().getId())
                .materialCode(materialInventory.getMaterial().getCode())
                .materialName(materialInventory.getMaterial().getName())
                .quantity(materialInventory.getQuantity())
                .build();

    }

    private LowStockResponse mapLowStock(
            MaterialInventory materialInventory
    ) {
        StockStatus status;

        if (materialInventory.getQuantity().compareTo(BigDecimal.ZERO) == 0) {

            status = StockStatus.OUT_OF_STOCK;

        } else if (materialInventory.getQuantity().compareTo(
                materialInventory.getMaterial().getMinimumStock()
        ) <= 0) {

            status = StockStatus.LOW;

        } else {

            status = StockStatus.NORMAL;

        }

        return LowStockResponse.builder()

                .inventoryId(
                        materialInventory.getId()
                )

                .warehouseId(
                        materialInventory.getWarehouse().getId()
                )

                .warehouse(
                        materialInventory.getWarehouse().getName()
                )

                .materialId(
                        materialInventory.getMaterial().getId()
                )

                .materialCode(
                        materialInventory.getMaterial().getCode()
                )

                .materialName(
                        materialInventory.getMaterial().getName()
                )

                .currentStock(
                        materialInventory.getQuantity()
                )

                .minimumStock(
                        materialInventory.getMaterial().getMinimumStock()
                )

                .unit(
                        materialInventory.getMaterial().getUnit()
                )
                .status(status)
                .build();

    }

}
