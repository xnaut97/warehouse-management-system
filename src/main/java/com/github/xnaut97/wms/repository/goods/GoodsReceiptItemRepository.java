package com.github.xnaut97.wms.repository.goods;

import com.github.xnaut97.wms.dto.dashboard.DashboardMonthlyQuantityResponse;
import com.github.xnaut97.wms.entity.goods.GoodsReceiptItem;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GoodsReceiptItemRepository
        extends JpaRepository<GoodsReceiptItem, Long> {

    List<GoodsReceiptItem> findByReceiptId(Long receiptId);

    boolean existsByReceiptIdAndMaterialId(Long receiptId, Long materialId);

    Optional<GoodsReceiptItem> findByIdAndReceiptId(
            Long id,
            Long receiptId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM GoodsReceiptItem i
            WHERE i.receipt.status = :status
            """)
    BigDecimal getTotalQuantityByReceiptStatus(

            @Param("status")
            ReceiptStatus status

    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.DashboardMonthlyQuantityResponse(
            YEAR(i.receipt.receiptDate),
            MONTH(i.receipt.receiptDate),
            COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsReceiptItem i
            WHERE i.receipt.status = :status
            AND i.receipt.receiptDate >= :fromDate
            GROUP BY YEAR(i.receipt.receiptDate),
                     MONTH(i.receipt.receiptDate)
            ORDER BY YEAR(i.receipt.receiptDate),
                     MONTH(i.receipt.receiptDate)
            """)
    List<DashboardMonthlyQuantityResponse> getMonthlyStockIn(

            @Param("status")
            ReceiptStatus status,

            @Param("fromDate")
            LocalDate fromDate

    );
}
