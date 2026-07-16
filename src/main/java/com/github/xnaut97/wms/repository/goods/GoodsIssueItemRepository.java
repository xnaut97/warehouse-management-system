package com.github.xnaut97.wms.repository.goods;

import com.github.xnaut97.wms.dto.dashboard.DashboardMonthlyQuantityResponse;
import com.github.xnaut97.wms.entity.goods.GoodsIssueItem;
import com.github.xnaut97.wms.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface GoodsIssueItemRepository
        extends JpaRepository<GoodsIssueItem, Long> {

    List<GoodsIssueItem> findByIssueId(Long issueId);

    boolean existsByIssueIdAndMaterialId(
            Long issueId,
            Long materialId
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM GoodsIssueItem i
            WHERE i.issue.status = :status
            """)
    BigDecimal getTotalQuantityByIssueStatus(

            @Param("status")
            IssueStatus status

    );

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.DashboardMonthlyQuantityResponse(
            YEAR(i.issue.issueDate),
            MONTH(i.issue.issueDate),
            COALESCE(SUM(i.quantity),0)
            )
            FROM GoodsIssueItem i
            WHERE i.issue.status = :status
            AND i.issue.issueDate >= :fromDate
            GROUP BY YEAR(i.issue.issueDate),
                     MONTH(i.issue.issueDate)
            ORDER BY YEAR(i.issue.issueDate),
                     MONTH(i.issue.issueDate)
            """)
    List<DashboardMonthlyQuantityResponse> getMonthlyStockOut(

            @Param("status")
            IssueStatus status,

            @Param("fromDate")
            LocalDate fromDate

    );

}
