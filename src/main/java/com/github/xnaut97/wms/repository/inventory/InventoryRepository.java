package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.dto.dashboard.DashboardReplenishmentRecommendationResponse;
import com.github.xnaut97.wms.dto.dashboard.LowStockMaterialResponse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends
        JpaRepository<Inventory, Long>,
        JpaSpecificationExecutor<Inventory> {

    Optional<Inventory> findByWarehouseIdAndMaterialId(
            Long warehouseId,
            Long materialId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM Inventory i
            """)
    BigDecimal getTotalQuantity();

    @Query("""
            SELECT COUNT(i)
            FROM Inventory i
            WHERE i.quantity <= i.material.minimumStock
            """)
    long countLowStock();

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM Inventory i
            """)
    BigDecimal getCurrentInventory();

    @Query("""
            SELECT i
            FROM Inventory i
            WHERE i.quantity <= i.material.minimumStock
            """)
    List<Inventory> findLowStockItems();

    @Query("""
            SELECT COALESCE(SUM(i.quantity * i.material.unitPrice),0)
            FROM Inventory i
            """)
    BigDecimal getTotalInventoryValue();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.LowStockMaterialResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.quantity),0),
            i.material.minimumStock,
            i.material.minimumStock - COALESCE(SUM(i.quantity),0)
            )
            FROM Inventory i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.minimumStock
            HAVING COALESCE(SUM(i.quantity),0) < i.material.minimumStock
            ORDER BY i.material.code
            """)
    List<LowStockMaterialResponse> findLowStockMaterials();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.DashboardReplenishmentRecommendationResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.quantity),0),
            i.material.minimumStock,
            i.material.minimumStock + i.material.minimumStock - COALESCE(SUM(i.quantity),0)
            )
            FROM Inventory i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.minimumStock
            HAVING COALESCE(SUM(i.quantity),0) < i.material.minimumStock
            ORDER BY i.material.code
            """)
    List<DashboardReplenishmentRecommendationResponse>
    findReplenishmentRecommendations();
}
