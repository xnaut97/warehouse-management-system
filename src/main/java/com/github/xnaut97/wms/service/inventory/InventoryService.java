package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.InventoryDetailResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryResponse;
import com.github.xnaut97.wms.dto.inventory.LowStockResponse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.enums.StockGroup;
import com.github.xnaut97.wms.enums.StockStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import com.github.xnaut97.wms.specification.InventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final MaterialInventoryRepository repository;

    private final ProductInventoryRepository productRepository;

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
