package com.github.xnaut97.wms.repository.alert;

import com.github.xnaut97.wms.dto.alert.AlertLotRow;
import com.github.xnaut97.wms.dto.alert.AlertStockRow;
import com.github.xnaut97.wms.dto.alert.AlertVarianceRow;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRepository
        extends JpaRepository<MaterialInventory, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertStockRow(
                i.material.id,
                i.material.code,
                i.material.name,
                i.material.unit,
                i.warehouse.id,
                i.warehouse.code,
                i.warehouse.name,
                COALESCE(SUM(i.quantity),0),
                COALESCE(SUM(i.quantity),0),
                i.material.minimumStock,
                i.material.maximumStock
            )
            FROM MaterialInventory i
            WHERE i.material.enabled = true
              AND i.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            GROUP BY i.material.id,
                     i.material.code,
                     i.material.name,
                     i.material.unit,
                     i.warehouse.id,
                     i.warehouse.code,
                     i.warehouse.name,
                     i.material.minimumStock,
                     i.material.maximumStock
            HAVING COALESCE(SUM(i.quantity),0) < i.material.minimumStock
                OR (i.material.maximumStock > 0
                    AND COALESCE(SUM(i.quantity),0) > i.material.maximumStock)
            ORDER BY i.material.code
            """)
    List<AlertStockRow> findMaterialThresholdRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertStockRow(
                i.product.id,
                i.product.code,
                i.product.name,
                i.product.unit,
                i.warehouse.id,
                i.warehouse.code,
                i.warehouse.name,
                COALESCE(SUM(i.quantity),0),
                COALESCE(SUM(i.quantity),0),
                i.product.minimumStock,
                i.product.maximumStock
            )
            FROM ProductInventory i
            WHERE i.product.enabled = true
              AND i.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            GROUP BY i.product.id,
                     i.product.code,
                     i.product.name,
                     i.product.unit,
                     i.warehouse.id,
                     i.warehouse.code,
                     i.warehouse.name,
                     i.product.minimumStock,
                     i.product.maximumStock
            HAVING COALESCE(SUM(i.quantity),0) < i.product.minimumStock
                OR (i.product.maximumStock > 0
                    AND COALESCE(SUM(i.quantity),0) > i.product.maximumStock)
            ORDER BY i.product.code
            """)
    List<AlertStockRow> findProductThresholdRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertLotRow(
                i.product.id,
                i.product.code,
                i.product.name,
                i.product.unit,
                i.warehouse.id,
                i.warehouse.code,
                i.warehouse.name,
                i.lotNumber,
                i.expirationDate,
                i.quantity,
                i.product.averagePrice
            )
            FROM ProductInventory i
            WHERE i.quantity > 0
              AND i.lotNumber IS NOT NULL
              AND i.lotNumber <> ''
              AND i.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
            """)
    List<AlertLotRow> findProductLotRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertLotRow(
                i.material.id,
                i.material.code,
                i.material.name,
                i.material.unit,
                r.warehouse.id,
                r.warehouse.code,
                r.warehouse.name,
                i.lotNumber,
                i.expirationDate,
                i.quantity,
                i.material.unitPrice
            )
            FROM GoodsReceiptItem i
            JOIN i.receipt r
            WHERE r.status = :status
              AND i.expirationDate IS NOT NULL
              AND i.lotNumber IS NOT NULL
              AND i.lotNumber <> ''
              AND i.quantity > 0
              AND r.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR r.warehouse.id = :warehouseId)
              AND EXISTS (
                    SELECT 1
                    FROM MaterialInventory mi
                    WHERE mi.material.id = i.material.id
                      AND mi.warehouse.id = r.warehouse.id
                      AND mi.quantity > 0
              )
            """)
    List<AlertLotRow> findMaterialLotRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertVarianceRow(
                i.material.code,
                i.material.name,
                i.material.unit,
                s.warehouse.id,
                s.warehouse.code,
                s.warehouse.name,
                s.stocktakingNo,
                s.stocktakingDate,
                s.status,
                i.systemQuantity,
                i.physicalQuantity,
                i.varianceQuantity
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            WHERE i.itemStatus = :status
              AND i.material IS NOT NULL
              AND s.status <> :balancedStatus
              AND s.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
            ORDER BY s.stocktakingDate DESC
            """)
    List<AlertVarianceRow> findMaterialVarianceRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("balancedStatus") StocktakingStatus balancedStatus,
            @Param("status") StocktakingItemStatus status,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.alert.AlertVarianceRow(
                i.product.code,
                i.product.name,
                i.product.unit,
                s.warehouse.id,
                s.warehouse.code,
                s.warehouse.name,
                s.stocktakingNo,
                s.stocktakingDate,
                s.status,
                b.lotNumber,
                b.expirationDate,
                b.systemQuantity,
                b.physicalQuantity,
                b.varianceQuantity
            )
            FROM StocktakingItemBatch b
            JOIN b.item i
            JOIN i.stocktaking s
            WHERE b.varianceQuantity <> 0
              AND i.product IS NOT NULL
              AND s.status <> :balancedStatus
              AND s.warehouse.code IN :warehouseCodes
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
            ORDER BY s.stocktakingDate DESC
            """)
    List<AlertVarianceRow> findProductBatchVarianceRows(
            @Param("warehouseCodes") List<String> warehouseCodes,
            @Param("balancedStatus") StocktakingStatus balancedStatus,
            @Param("warehouseId") Long warehouseId
    );

}
