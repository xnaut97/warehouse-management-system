package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse;
import com.github.xnaut97.wms.dto.report.operation.MaterialQuantityResponse;
import com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface OperationReportRepository
        extends JpaRepository<GoodsIssue, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MaterialQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                m.unitPrice,
                COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsIssueItem i
            JOIN i.issue g
            JOIN i.material m
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY m.id, m.code, m.name, m.unit, m.unitPrice
            ORDER BY m.code
            """)
    List<MaterialQuantityResponse> getActualMaterialConsumption(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MaterialQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                m.unitPrice,
                COALESCE(SUM(i.quantity * bi.consumptionQuantity),0)
            )
            FROM ProductReceiptItem i
            JOIN i.receipt r
            JOIN BOM b
                ON b.product = i.product
                AND b.enabled = true
            JOIN b.items bi
            JOIN bi.material m
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY m.id, m.code, m.name, m.unit, m.unitPrice
            ORDER BY m.code
            """)
    List<MaterialQuantityResponse> getStandardMaterialConsumption(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(r)
            FROM GoodsReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            """)
    long countMaterialReceipts(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(r)
            FROM ProductReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            """)
    long countProductReceipts(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(g)
            FROM GoodsIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            """)
    long countMaterialIssues(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT COUNT(g)
            FROM ProductIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            """)
    long countProductIssues(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse(
                YEAR(r.receiptDate),
                MONTH(r.receiptDate),
                COUNT(r)
            )
            FROM GoodsReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            """)
    List<MonthlyCountResponse> getMonthlyMaterialReceiptCount(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse(
                YEAR(r.receiptDate),
                MONTH(r.receiptDate),
                COUNT(r)
            )
            FROM ProductReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            """)
    List<MonthlyCountResponse> getMonthlyProductReceiptCount(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse(
                YEAR(g.issueDate),
                MONTH(g.issueDate),
                COUNT(g)
            )
            FROM GoodsIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
            """)
    List<MonthlyCountResponse> getMonthlyMaterialIssueCount(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse(
                YEAR(g.issueDate),
                MONTH(g.issueDate),
                COUNT(g)
            )
            FROM ProductIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
            """)
    List<MonthlyCountResponse> getMonthlyProductIssueCount(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse(
                r.receiptDate,
                COUNT(r)
            )
            FROM GoodsReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY r.receiptDate
            """)
    List<DocumentDateCountResponse> getMaterialReceiptCountByDate(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse(
                r.receiptDate,
                COUNT(r)
            )
            FROM ProductReceipt r
            WHERE r.status = :status
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY r.receiptDate
            """)
    List<DocumentDateCountResponse> getProductReceiptCountByDate(
            @Param("status") ReceiptStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse(
                g.issueDate,
                COUNT(g)
            )
            FROM GoodsIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY g.issueDate
            """)
    List<DocumentDateCountResponse> getMaterialIssueCountByDate(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse(
                g.issueDate,
                COUNT(g)
            )
            FROM ProductIssue g
            WHERE g.status = :status
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY g.issueDate
            """)
    List<DocumentDateCountResponse> getProductIssueCountByDate(
            @Param("status") IssueStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
