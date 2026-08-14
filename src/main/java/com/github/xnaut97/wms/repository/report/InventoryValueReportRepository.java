package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InventoryValueReportRepository
        extends JpaRepository<MaterialInventory, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(r.receiptDate),
                MONTH(r.receiptDate),
                COALESCE(SUM(i.quantity * m.unitPrice),0)
            )
            FROM GoodsReceiptItem i
            JOIN i.receipt r
            JOIN i.material m
            WHERE r.status = :status
              AND r.receiptDate >= :fromDate
            GROUP BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            """)
    List<MonthlyValueResponse> getMonthlyMaterialInValue(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(g.issueDate),
                MONTH(g.issueDate),
                COALESCE(SUM(i.quantity * m.unitPrice),0)
            )
            FROM GoodsIssueItem i
            JOIN i.issue g
            JOIN i.material m
            WHERE g.status = :status
              AND g.issueDate >= :fromDate
            GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
            """)
    List<MonthlyValueResponse> getMonthlyMaterialOutValue(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(r.receiptDate),
                MONTH(r.receiptDate),
                COALESCE(SUM(i.quantity * p.averagePrice),0)
            )
            FROM ProductReceiptItem i
            JOIN i.receipt r
            JOIN i.product p
            WHERE r.status = :status
              AND r.receiptDate >= :fromDate
            GROUP BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            """)
    List<MonthlyValueResponse> getMonthlyProductInValue(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(g.issueDate),
                MONTH(g.issueDate),
                COALESCE(SUM(i.quantity * p.averagePrice),0)
            )
            FROM ProductIssueItem i
            JOIN i.issue g
            JOIN i.product p
            WHERE g.status = :status
              AND g.issueDate >= :fromDate
            GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
            """)
    List<MonthlyValueResponse> getMonthlyProductOutValue(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(s.stocktakingDate),
                MONTH(s.stocktakingDate),
                COALESCE(SUM(i.varianceQuantity * m.unitPrice),0)
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            JOIN i.material m
            WHERE s.status = :status
              AND s.stocktakingDate >= :fromDate
            GROUP BY YEAR(s.stocktakingDate), MONTH(s.stocktakingDate)
            """)
    List<MonthlyValueResponse> getMonthlyMaterialAdjustmentValue(
            @Param("status") StocktakingStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.value.MonthlyValueResponse(
                YEAR(s.stocktakingDate),
                MONTH(s.stocktakingDate),
                COALESCE(SUM(i.varianceQuantity * p.averagePrice),0)
            )
            FROM StocktakingItem i
            JOIN i.stocktaking s
            JOIN i.product p
            WHERE s.status = :status
              AND s.stocktakingDate >= :fromDate
            GROUP BY YEAR(s.stocktakingDate), MONTH(s.stocktakingDate)
            """)
    List<MonthlyValueResponse> getMonthlyProductAdjustmentValue(
            @Param("status") StocktakingStatus status,
            @Param("fromDate") LocalDate fromDate
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity * m.unitPrice),0)
            FROM GoodsIssueItem i
            JOIN i.issue g
            JOIN i.material m
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            """)
    BigDecimal getMaterialIssuedValue(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity * p.averagePrice),0)
            FROM ProductIssueItem i
            JOIN i.issue g
            JOIN i.product p
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            """)
    BigDecimal getProductIssuedValue(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
