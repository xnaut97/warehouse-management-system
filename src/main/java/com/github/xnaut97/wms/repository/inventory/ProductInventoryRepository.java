package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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

    List<ProductInventory> findAllByWarehouseIdOrderByProductCodeAscLotNumberAsc(
            Long warehouseId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity * i.product.averagePrice),0)
            FROM ProductInventory i
            """)
    BigDecimal getTotalInventoryValue();
}
