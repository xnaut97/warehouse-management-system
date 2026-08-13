package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.InventoryDetailResponse;
import com.github.xnaut97.wms.dto.inventory.InventoryResponse;
import com.github.xnaut97.wms.dto.inventory.LowStockResponse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.enums.StockStatus;
import com.github.xnaut97.wms.exception.BusinessException;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
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

    private final MaterialInventoryRepository repository;

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

    private InventoryResponse map(
            MaterialInventory materialInventory
    ) {

        return InventoryResponse.builder()
                .id(materialInventory.getId())
                .warehouseId(materialInventory.getWarehouse().getId())
                .warehouse(materialInventory.getWarehouse().getName())
                .materialId(materialInventory.getMaterial().getId())
                .materialCode(materialInventory.getMaterial().getCode())
                .materialName(materialInventory.getMaterial().getName())
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