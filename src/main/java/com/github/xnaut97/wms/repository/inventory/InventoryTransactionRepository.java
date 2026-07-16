package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.dto.dashboard.SlowMovingMaterialResponse;
import com.github.xnaut97.wms.entity.inventory.InventoryTransaction;
import com.github.xnaut97.wms.enums.InventoryTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>,
        JpaSpecificationExecutor<InventoryTransaction> {

    List<InventoryTransaction> findByWarehouseIdAndMaterialIdOrderByCreatedAtAsc(
            Long warehouseId,
            Long materialId
    );

    @Query("""
            SELECT t
            FROM InventoryTransaction t
            WHERE t.warehouse.id = :warehouseId
            AND t.material.id = :materialId
            AND (:from IS NULL OR t.createdAt >= :from)
            AND (:to IS NULL OR t.createdAt <= :to)
            ORDER BY t.createdAt
            """)
    List<InventoryTransaction> findStockCard(
            Long warehouseId,
            Long materialId,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
            SELECT COALESCE(SUM(t.quantity),0)
            FROM InventoryTransaction t
            WHERE t.type = 'IN'
            """)
    BigDecimal getTotalStockIn();

    @Query("""
            SELECT COALESCE(SUM(t.quantity),0)
            FROM InventoryTransaction t
            WHERE t.type = 'OUT'
            """)
    BigDecimal getTotalStockOut();

    @Query("""
            SELECT
            YEAR(t.createdAt),
            MONTH(t.createdAt),
            SUM(
            CASE
            WHEN t.type='IN'
            THEN t.quantity
            ELSE 0
            END
            ),
            SUM(
            CASE
            WHEN t.type='OUT'
            THEN t.quantity
            ELSE 0
            END
            )
            FROM InventoryTransaction t
            GROUP BY YEAR(t.createdAt), MONTH(t.createdAt)
            ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)
            """)
    List<Object[]> inventoryTrend();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.SlowMovingMaterialResponse(
            m.code,
            m.name,
            (
                SELECT COALESCE(SUM(i.quantity),0)
                FROM Inventory i
                WHERE i.material = m
            ),
            (
                SELECT MAX(t.createdAt)
                FROM InventoryTransaction t
                WHERE t.material = m
                AND t.type = :type
            )
            )
            FROM RawMaterial m
            WHERE NOT EXISTS (
                SELECT recentTransaction
                FROM InventoryTransaction recentTransaction
                WHERE recentTransaction.material = m
                AND recentTransaction.type = :type
                AND recentTransaction.createdAt >= :cutoff
            )
            ORDER BY m.code
            """)
    List<SlowMovingMaterialResponse> findSlowMovingMaterials(

            @Param("type")
            InventoryTransactionType type,

            @Param("cutoff")
            LocalDateTime cutoff

    );
}
