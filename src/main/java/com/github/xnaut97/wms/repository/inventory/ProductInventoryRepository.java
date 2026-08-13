package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductInventoryRepository
        extends JpaRepository<ProductInventory, Long> {

    Optional<ProductInventory> findByWarehouseIdAndProductId(
            Long warehouseId,
            Long productId
    );

    List<ProductInventory> findAllByWarehouseIdAndProductId(
            Long warehouseId,
            Long productId
    );
}