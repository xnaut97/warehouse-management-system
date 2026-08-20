package com.github.xnaut97.wms.service.inventory;

import com.github.xnaut97.wms.dto.inventory.ProductStockResponse;
import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import com.github.xnaut97.wms.repository.inventory.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final ProductInventoryRepository repository;

    /**
     * Lots of a product that still have stock in the given warehouse,
     * ordered by expiration date first so the UI can suggest FEFO.
     */
    @Transactional(readOnly = true)
    public List<ProductStockResponse> getAvailableLots(
            Long warehouseId,
            Long productId
    ) {

        return repository.findAvailableLots(
                        warehouseId,
                        productId
                )
                .stream()
                .map(this::map)
                .toList();
    }

    private ProductStockResponse map(
            ProductInventory inventory
    ) {

        return ProductStockResponse.builder()
                .inventoryId(inventory.getId())
                .warehouseId(inventory.getWarehouse().getId())
                .productId(inventory.getProduct().getId())
                .lotNumber(inventory.getLotNumber())
                .expirationDate(inventory.getExpirationDate())
                .quantity(inventory.getQuantity())
                .build();
    }
}
