package com.github.xnaut97.wms.repository.goods;

import com.github.xnaut97.wms.dto.report.receipt.ReceiptDailyReportResponse;
import com.github.xnaut97.wms.entity.goods.GoodsReceipt;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GoodsReceiptRepository
        extends JpaRepository<GoodsReceipt, Long> {

    boolean existsByReceiptNo(String receiptNo);

    Page<GoodsReceipt> findByReceiptNoContainingIgnoreCase(
            String receiptNo,
            Pageable pageable
    );

    long countByReceiptDateBetween(
            LocalDate start,
            LocalDate end
    );

    @Query("""
            SELECT YEAR(r.receiptDate),
                   MONTH(r.receiptDate),
                   COUNT(r)
            FROM GoodsReceipt r
            GROUP BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            ORDER BY YEAR(r.receiptDate), MONTH(r.receiptDate)
            """)
    List<Object[]> monthlyReceiptStatistics();

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

            @Param("date")
            LocalDate date,

            @Param("status")
            ReceiptStatus status

    );

    @Query("""
            SELECT COUNT(r)
            FROM GoodsReceipt r
            WHERE r.status = :status
            """)
    long countByStatus(

            @Param("status")
            ReceiptStatus status

    );

}
