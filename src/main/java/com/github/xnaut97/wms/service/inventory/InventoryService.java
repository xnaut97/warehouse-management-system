package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.InventoryDetailResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryResponse;
import com.github.xnaut97.wms.dto.inventory.LowStockResponse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import com.github.xnaut97.wms.enums.StockStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import com.github.xnaut97.wms.specification.InventorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;

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

        return repository.findAll(

                InventorySpecification.filter(

                        warehouseId,

                        materialId,

                        keyword

                ),

                pageable

        ).map(this::map);

    }

    @Transactional
    public InventoryDetailResponse getDetail(
            Long id
    ) {

        Inventory inventory = findInventoryById(id);

        return mapDetail(inventory);

    }

    public Inventory findInventoryById(
            Long id
    ) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(
                                "Không tìm thấy tồn kho."
                        ));

    }

    private InventoryResponse map(
            Inventory inventory
    ) {

        return InventoryResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .warehouse(inventory.getWarehouse().getName())
                .materialId(inventory.getMaterial().getId())
                .materialCode(inventory.getMaterial().getCode())
                .materialName(inventory.getMaterial().getName())
                .quantity(inventory.getQuantity())
                .build();

    }

    private InventoryDetailResponse mapDetail(
            Inventory inventory
    ) {

        return InventoryDetailResponse.builder()
                .id(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .warehouse(inventory.getWarehouse().getName())
                .materialId(inventory.getMaterial().getId())
                .materialCode(inventory.getMaterial().getCode())
                .materialName(inventory.getMaterial().getName())
                .quantity(inventory.getQuantity())
                .build();

    }

    private LowStockResponse mapLowStock(
            Inventory inventory
    ) {
        StockStatus status;

        if (inventory.getQuantity().compareTo(BigDecimal.ZERO) == 0) {

            status = StockStatus.OUT_OF_STOCK;

        } else if (inventory.getQuantity().compareTo(
                inventory.getMaterial().getMinimumStock()
        ) <= 0) {

            status = StockStatus.LOW;

        } else {

            status = StockStatus.NORMAL;

        }

        return LowStockResponse.builder()

                .inventoryId(
                        inventory.getId()
                )

                .warehouseId(
                        inventory.getWarehouse().getId()
                )

                .warehouse(
                        inventory.getWarehouse().getName()
                )

                .materialId(
                        inventory.getMaterial().getId()
                )

                .materialCode(
                        inventory.getMaterial().getCode()
                )

                .materialName(
                        inventory.getMaterial().getName()
                )

                .currentStock(
                        inventory.getQuantity()
                )

                .minimumStock(
                        inventory.getMaterial().getMinimumStock()
                )

                .unit(
                        inventory.getMaterial().getUnit()
                )
                .status(status)
                .build();

    }

}