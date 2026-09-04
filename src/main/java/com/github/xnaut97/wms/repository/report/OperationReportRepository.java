package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.operation.DocumentDateCountResponse;
import com.github.xnaut97.wms.dto.report.operation.MaterialQuantityResponse;
import com.github.xnaut97.wms.dto.report.operation.MonthlyCountResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse;
import com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse;
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
              AND g.customer IS NULL
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


    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                COALESCE(SUM(inv.quantity),0)
            )
            FROM MaterialInventory inv
            JOIN inv.material m
            WHERE inv.warehouse.id = :warehouseId
            GROUP BY m.id, m.code, m.name, m.unit
            """)
    List<OperationQuantityResponse> getMaterialCurrentStock(
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                p.id,
                p.code,
                p.name,
                p.unit,
                COALESCE(SUM(inv.quantity),0)
            )
            FROM ProductInventory inv
            JOIN inv.product p
            WHERE inv.warehouse.id = :warehouseId
            GROUP BY p.id, p.code, p.name, p.unit
            """)
    List<OperationQuantityResponse> getProductCurrentStock(
            @Param("warehouseId") Long warehouseId
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsReceiptItem i
            JOIN i.receipt r
            JOIN i.material m
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY m.id, m.code, m.name, m.unit
            """)
    List<OperationQuantityResponse> getMaterialReceiptQuantities(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsReceiptItem i
            JOIN i.receipt r
            JOIN i.material m
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate > :date
            GROUP BY m.id, m.code, m.name, m.unit
            """)
    List<OperationQuantityResponse> getMaterialReceiptQuantitiesAfter(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsIssueItem i
            JOIN i.issue g
            JOIN i.material m
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY m.id, m.code, m.name, m.unit
            """)
    List<OperationQuantityResponse> getMaterialIssueQuantities(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                m.id,
                m.code,
                m.name,
                m.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsIssueItem i
            JOIN i.issue g
            JOIN i.material m
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate > :date
            GROUP BY m.id, m.code, m.name, m.unit
            """)
    List<OperationQuantityResponse> getMaterialIssueQuantitiesAfter(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                p.id,
                p.code,
                p.name,
                p.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM ProductReceiptItem i
            JOIN i.receipt r
            JOIN i.product p
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY p.id, p.code, p.name, p.unit
            """)
    List<OperationQuantityResponse> getProductReceiptQuantities(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                p.id,
                p.code,
                p.name,
                p.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM ProductReceiptItem i
            JOIN i.receipt r
            JOIN i.product p
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate > :date
            GROUP BY p.id, p.code, p.name, p.unit
            """)
    List<OperationQuantityResponse> getProductReceiptQuantitiesAfter(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                p.id,
                p.code,
                p.name,
                p.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM ProductIssueItem i
            JOIN i.issue g
            JOIN i.product p
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY p.id, p.code, p.name, p.unit
            """)
    List<OperationQuantityResponse> getProductIssueQuantities(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationQuantityResponse(
                p.id,
                p.code,
                p.name,
                p.unit,
                COALESCE(SUM(i.quantity),0)
            )
            FROM ProductIssueItem i
            JOIN i.issue g
            JOIN i.product p
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate > :date
            GROUP BY p.id, p.code, p.name, p.unit
            """)
    List<OperationQuantityResponse> getProductIssueQuantitiesAfter(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse(
                i.material.id,
                r.id,
                r.receiptNo,
                r.receiptDate,
                COALESCE(SUM(i.quantity),0),
                s.code,
                s.name
            )
            FROM GoodsReceiptItem i
            JOIN i.receipt r
            LEFT JOIN r.supplier s
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY i.material.id, r.id, r.receiptNo, r.receiptDate, s.code, s.name
            """)
    List<OperationDocumentResponse> getMaterialReceiptDocuments(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse(
                i.material.id,
                g.id,
                g.issueNo,
                g.issueDate,
                COALESCE(SUM(i.quantity),0),
                c.code,
                c.name
            )
            FROM GoodsIssueItem i
            JOIN i.issue g
            LEFT JOIN g.customer c
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY i.material.id, g.id, g.issueNo, g.issueDate, c.code, c.name
            """)
    List<OperationDocumentResponse> getMaterialIssueDocuments(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse(
                i.product.id,
                r.id,
                r.receiptNo,
                r.receiptDate,
                COALESCE(SUM(i.quantity),0),
                s.code,
                s.name
            )
            FROM ProductReceiptItem i
            JOIN i.receipt r
            LEFT JOIN r.supplier s
            WHERE r.status = :status
              AND r.warehouse.id = :warehouseId
              AND r.receiptDate BETWEEN :fromDate AND :toDate
            GROUP BY i.product.id, r.id, r.receiptNo, r.receiptDate, s.code, s.name
            """)
    List<OperationDocumentResponse> getProductReceiptDocuments(
            @Param("status") ReceiptStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.operation.OperationDocumentResponse(
                i.product.id,
                g.id,
                g.issueNo,
                g.issueDate,
                COALESCE(SUM(i.quantity),0),
                c.code,
                c.name
            )
            FROM ProductIssueItem i
            JOIN i.issue g
            LEFT JOIN g.customer c
            WHERE g.status = :status
              AND g.warehouse.id = :warehouseId
              AND g.issueDate BETWEEN :fromDate AND :toDate
            GROUP BY i.product.id, g.id, g.issueNo, g.issueDate, c.code, c.name
            """)
    List<OperationDocumentResponse> getProductIssueDocuments(
            @Param("status") IssueStatus status,
            @Param("warehouseId") Long warehouseId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

}
