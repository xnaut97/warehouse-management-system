package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingSummaryReportResponse;
import com.github.xnaut97.wms.dto.report.stocktaking.StocktakingVarianceReportResponse;
import com.github.xnaut97.wms.entity.stock.Stocktaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    List<StocktakingReportResponse> getStocktakingReport();

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
    List<StocktakingVarianceReportResponse> getVarianceReport();

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
}