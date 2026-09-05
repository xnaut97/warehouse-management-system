package com.github.xnaut97.wms.repository.product;

import com.github.xnaut97.wms.dto.dashboard.SlowMovingMaterialResponse;
import com.github.xnaut97.wms.entity.product.ProductIssueItem;
import com.github.xnaut97.wms.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductIssueItemRepository extends JpaRepository<ProductIssueItem, Long> {

    List<ProductIssueItem> findByIssueId(Long issueId);

    @Query("""
            SELECT SUM(i.quantity * i.unitPrice) / NULLIF(SUM(i.quantity), 0)
            FROM ProductIssueItem i
            WHERE i.product.id = :productId
              AND i.unitPrice IS NOT NULL
              AND i.quantity > 0
              AND i.issue.status = :status
            """)
    BigDecimal calculateAveragePrice(
            @Param("productId") Long productId,
            @Param("status") IssueStatus status
    );

    @Query("""
            SELECT COALESCE(SUM(i.quantity),0)
            FROM ProductIssueItem i
            """)
    BigDecimal getTotalQuantity();

    @Query("""
            SELECT new com.github.xnaut97.wms.dto.dashboard.SlowMovingMaterialResponse(
            p.code,
            p.name,
            (
                SELECT COALESCE(SUM(inv.quantity),0)
                FROM ProductInventory inv
                WHERE inv.product = p
            ),
            (
                SELECT MAX(i.issue.createdAt)
                FROM ProductIssueItem i
                WHERE i.product = p
                AND i.issue.status = :status
            )
            )
            FROM Product p
            WHERE NOT EXISTS (
                SELECT recentItem
                FROM ProductIssueItem recentItem
                WHERE recentItem.product = p
                AND recentItem.issue.status = :status
                AND recentItem.issue.createdAt >= :cutoff
            )
            ORDER BY p.code
            """)
    List<SlowMovingMaterialResponse> findSlowMovingProducts(
            @Param("status") IssueStatus status,
            @Param("cutoff") LocalDateTime cutoff
    );

}
