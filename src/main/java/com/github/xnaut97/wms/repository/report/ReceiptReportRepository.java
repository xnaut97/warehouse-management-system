package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.receipt.ReceiptDailyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptMonthlyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptSupplierReportResponse;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReceiptReportRepository
        extends JpaRepository<GoodsReceipt, Long> {

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.report.receipt.ReceiptDailyReportResponse(
                :date,
                COUNT(DISTINCT r.id),
                COALESCE(SUM(i.quantity),0),
                COALESCE(SUM(i.amount),0)
            )
            FROM GoodsReceipt r
            LEFT JOIN GoodsReceiptItem i
                ON i.receipt = r
            WHERE r.status = :status
              AND r.receiptDate = :date
            """)
    ReceiptDailyReportResponse getDailyReport(
            @Param("date") LocalDate date,
            @Param("status") ReceiptStatus status
    );

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.receipt.ReceiptMonthlyReportResponse(
            MONTH(r.receiptDate),
            COUNT(DISTINCT r.id),
            COALESCE(SUM(i.quantity),0),
            COALESCE(SUM(i.amount),0)
        )
        FROM GoodsReceipt r
        LEFT JOIN GoodsReceiptItem i
            ON i.receipt = r
        WHERE r.status = :status
          AND YEAR(r.receiptDate) = :year
        GROUP BY MONTH(r.receiptDate)
        ORDER BY MONTH(r.receiptDate)
        """)
    List<ReceiptMonthlyReportResponse> getMonthlyReport(
            @Param("year") int year,
            @Param("status") ReceiptStatus status
    );

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.receipt.ReceiptSupplierReportResponse(
            s.id,
            s.name,
            COUNT(DISTINCT r.id),
            COALESCE(SUM(i.quantity),0),
            COALESCE(SUM(i.amount),0)
        )
        FROM GoodsReceipt r
        JOIN r.supplier s
        LEFT JOIN GoodsReceiptItem i
            ON i.receipt = r
        WHERE r.status = :status
          AND r.receiptDate BETWEEN :fromDate AND :toDate
        GROUP BY s.id, s.name
        ORDER BY s.name
        """)
    List<ReceiptSupplierReportResponse> getSupplierReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") ReceiptStatus status
    );

}