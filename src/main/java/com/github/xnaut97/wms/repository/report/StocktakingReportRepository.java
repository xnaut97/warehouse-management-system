package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingAccuracyTotalsResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReasonRow;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingWarehouseVarianceRow;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import com.github.xnaut97.wms.enums.StocktakingItemStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StocktakingReportRepository extends JpaRepository<Stocktaking, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse(
            
                s.stocktakingNo,
            
                s.warehouse.name,
            
                s.stocktakingDate,
            
                s.status,
            
                s.createdBy.username
            
            )
            
            FROM Stocktaking s
            
            ORDER BY s.stocktakingDate DESC
            """)
    Page<StocktakingReportResponse> getStocktakingReport(Pageable pageable);

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse(
            
                s.stocktakingNo,
            
                m.code,
            
                m.name,
            
                i.systemQuantity,
            
                i.physicalQuantity,
            
                i.varianceQuantity,
            
                ABS(i.varianceQuantity) * m.unitPrice
            
            )
            
            FROM StocktakingItem i
            
            JOIN i.stocktaking s
            
            JOIN i.material m
            
            WHERE i.varianceQuantity <> 0
            
            ORDER BY s.stocktakingDate DESC
            """)
    Page<StocktakingVarianceReportResponse> getVarianceReport(Pageable pageable);

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse(
            
                COUNT(DISTINCT s.id),
            
                COUNT(i.id),
            
                COALESCE(SUM(ABS(i.varianceQuantity)),0),
            
                COALESCE(SUM(ABS(i.varianceQuantity) * m.unitPrice),0)
            
            )
            
            FROM StocktakingItem i
            
            JOIN i.stocktaking s
            
            JOIN i.material m
            
            WHERE i.varianceQuantity <> 0
            """)
    StocktakingSummaryReportResponse getSummary();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingAccuracyTotalsResponse(
                COALESCE(SUM(i.systemQuantity),0),
                COALESCE(SUM(i.physicalQuantity),0),
                COALESCE(SUM(i.varianceQuantity),0),
                COALESCE(SUM(ABS(i.varianceQuantity)),0),
                COALESCE(SUM(i.varianceQuantity * COALESCE(m.unitPrice, p.averagePrice, 0)),0),
                COALESCE(SUM(ABS(i.varianceQuantity) * COALESCE(m.unitPrice, p.averagePrice, 0)),0),
                COUNT(i.id),
                COALESCE(SUM(CASE WHEN i.itemStatus = :discrepancyStatus THEN 1 ELSE 0 END),0)
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            LEFT JOIN i.material m
            LEFT JOIN i.product p
            WHERE s.status IN :statuses
              AND s.stocktakingDate BETWEEN :fromDate AND :toDate
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
            """)
    StocktakingAccuracyTotalsResponse getAccuracyTotals(
            @Param("statuses") Collection<StocktakingStatus> statuses,
            @Param("discrepancyStatus") StocktakingItemStatus discrepancyStatus,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT COUNT(s)
            FROM Stocktaking s
            WHERE s.status IN :statuses
              AND s.stocktakingDate BETWEEN :fromDate AND :toDate
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
            """)
    long countCompletedStocktakings(
            @Param("statuses") Collection<StocktakingStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingWarehouseVarianceRow(
                w.id,
                w.code,
                w.name,
                COUNT(i.id),
                COALESCE(SUM(i.varianceQuantity),0),
                COALESCE(SUM(ABS(i.varianceQuantity)),0),
                COALESCE(SUM(i.varianceQuantity * COALESCE(m.unitPrice, p.averagePrice, 0)),0),
                COALESCE(SUM(ABS(i.varianceQuantity) * COALESCE(m.unitPrice, p.averagePrice, 0)),0),
                COALESCE(SUM(CASE WHEN p.id IS NOT NULL THEN 1 ELSE 0 END),0)
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            JOIN s.warehouse w
            LEFT JOIN i.material m
            LEFT JOIN i.product p
            WHERE s.status IN :statuses
              AND s.stocktakingDate BETWEEN :fromDate AND :toDate
              AND (:warehouseId IS NULL OR w.id = :warehouseId)
              AND i.varianceQuantity <> 0
            GROUP BY w.id, w.code, w.name
            ORDER BY w.code
            """)
    List<StocktakingWarehouseVarianceRow> getVarianceByWarehouse(
            @Param("statuses") Collection<StocktakingStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReasonRow(
                i.reason,
                COUNT(i.id),
                COALESCE(SUM(ABS(i.varianceQuantity)),0),
                COALESCE(SUM(ABS(i.varianceQuantity) * COALESCE(m.unitPrice, 0)),0)
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            LEFT JOIN i.material m
            WHERE s.status IN :statuses
              AND s.stocktakingDate BETWEEN :fromDate AND :toDate
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
              AND i.product IS NULL
              AND i.varianceQuantity <> 0
            GROUP BY i.reason
            """)
    List<StocktakingReasonRow> getMaterialItemReasons(
            @Param("statuses") Collection<StocktakingStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReasonRow(
                b.reason,
                COUNT(b.id),
                COALESCE(SUM(ABS(b.varianceQuantity)),0),
                COALESCE(SUM(ABS(b.varianceQuantity) * COALESCE(p.averagePrice, 0)),0)
            )
            FROM StocktakingItemBatch b
            JOIN b.item i
            JOIN i.stocktaking s
            LEFT JOIN i.product p
            WHERE s.status IN :statuses
              AND s.stocktakingDate BETWEEN :fromDate AND :toDate
              AND (:warehouseId IS NULL OR s.warehouse.id = :warehouseId)
              AND b.varianceQuantity <> 0
            GROUP BY b.reason
            """)
    List<StocktakingReasonRow> getProductBatchReasons(
            @Param("statuses") Collection<StocktakingStatus> statuses,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );
}
