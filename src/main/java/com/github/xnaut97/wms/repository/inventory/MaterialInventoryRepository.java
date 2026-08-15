package com.github.xnaut97.wms.repository.inventory;

import com.github.xnaut97.wms.dto.dashboard.AboveMaxAlertResponse;
import com.github.xnaut97.wms.dto.dashboard.BelowMinAlertResponse;
import com.github.xnaut97.wms.dto.dashboard.DashboardReplenishmentRecommendationResponse;
import com.github.xnaut97.wms.dto.dashboard.LowStockMaterialResponse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MaterialInventoryRepository extends
        JpaRepository<MaterialInventory, Long>,
        JpaSpecificationExecutor<MaterialInventory> {

    Optional<MaterialInventory> findByWarehouseIdAndMaterialId(
            Long warehouseId,
            Long materialId
    );

    List<MaterialInventory> findAllByWarehouseIdOrderByMaterialCodeAsc(
            Long warehouseId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM MaterialInventory i
            """)
    BigDecimal getTotalQuantity();

    @Query("""
            SELECT COUNT(i)
            FROM MaterialInventory i
            WHERE i.quantity <= i.material.minimumStock
            """)
    long countLowStock();

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM MaterialInventory i
            """)
    BigDecimal getCurrentInventory();

    @Query("""
            SELECT i
            FROM MaterialInventory i
            WHERE i.quantity <= i.material.minimumStock
            """)
    List<MaterialInventory> findLowStockItems();

    @Query("""
            SELECT COALESCE(SUM(i.quantity * i.material.unitPrice),0)
            FROM MaterialInventory i
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
            FROM MaterialInventory i
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
            FROM MaterialInventory i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.minimumStock
            HAVING COALESCE(SUM(i.quantity),0) < i.material.minimumStock
            ORDER BY i.material.code
            """)
    List<DashboardReplenishmentRecommendationResponse>
    findReplenishmentRecommendations();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.BelowMinAlertResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.quantity),0),
            i.material.minimumStock,
            i.material.unit
            )
            FROM MaterialInventory i
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.minimumStock,
                     i.material.unit
            HAVING COALESCE(SUM(i.quantity),0) < i.material.minimumStock
            ORDER BY i.material.code
            """)
    List<BelowMinAlertResponse> findBelowMinAlerts();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.AboveMaxAlertResponse(
            i.material.code,
            i.material.name,
            COALESCE(SUM(i.quantity),0),
            i.material.maximumStock,
            i.material.unit
            )
            FROM MaterialInventory i
            WHERE i.material.maximumStock > 0
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.maximumStock,
                     i.material.unit
            HAVING COALESCE(SUM(i.quantity),0) > i.material.maximumStock
            ORDER BY i.material.code
            """)
    List<AboveMaxAlertResponse> findAboveMaxAlerts();
}
