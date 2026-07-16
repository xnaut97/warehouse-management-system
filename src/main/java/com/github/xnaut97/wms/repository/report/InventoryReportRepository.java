package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.inventory.InventoryHistoryResponse;
import com.github.xnaut97.wms.dto.report.inventory.InventoryReportResponse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryReportRepository
        extends JpaRepository<Inventory, Long> {

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.inventory.InventoryReportResponse(
            i.warehouse.id,
            i.warehouse.name,
            i.material.id,
            i.material.code,
            i.material.name,
            i.material.unit,
            i.quantity,
            i.material.minimumStock,
            i.quantity * i.material.unitPrice,
            CASE
                WHEN i.quantity = 0 THEN 'OUT_OF_STOCK'
                WHEN i.quantity <= i.material.minimumStock THEN 'LOW_STOCK'
                ELSE 'NORMAL'
            END
        )
        FROM Inventory i
        ORDER BY i.material.code
        """)
    List<InventoryReportResponse> getRawMaterialInventory();

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.inventory.InventoryHistoryResponse(
            t.createdAt,
            t.referenceNo,
            t.type,
            t.quantity,
            t.warehouse.name,
            t.material.code,
            t.material.name
        )
        FROM InventoryTransaction t
        WHERE
            (:warehouseId IS NULL OR t.warehouse.id = :warehouseId)
        AND
            (:from IS NULL OR t.createdAt >= :from)
        AND
            (:to IS NULL OR t.createdAt <= :to)
        ORDER BY t.createdAt DESC
        """)
    List<InventoryHistoryResponse> getHistory(
            @Param("warehouseId") Long warehouseId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

}