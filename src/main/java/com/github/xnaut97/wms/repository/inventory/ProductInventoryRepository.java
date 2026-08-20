package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.entity.inventory.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            SELECT i
            FROM ProductInventory i
            WHERE i.warehouse.id = :warehouseId
              AND i.product.id = :productId
              AND (
                    (:lotNumber IS NULL AND i.lotNumber IS NULL)
                    OR i.lotNumber = :lotNumber
              )
            """)
    Optional<ProductInventory> findByWarehouseProductAndLot(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId,
            @Param("lotNumber") String lotNumber
    );

    @Query("""
            SELECT i
            FROM ProductInventory i
            WHERE i.warehouse.id = :warehouseId
              AND i.product.id = :productId
              AND i.quantity > 0
            ORDER BY
                CASE WHEN i.expirationDate IS NULL THEN 1 ELSE 0 END ASC,
                i.expirationDate ASC,
                i.lotNumber ASC
            """)
    List<ProductInventory> findAvailableLots(
            @Param("warehouseId") Long warehouseId,
            @Param("productId") Long productId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity * i.product.averagePrice),0)
            FROM ProductInventory i
            """)
    BigDecimal getTotalInventoryValue();
}
