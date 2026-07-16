package com.github.xnaut97.wms.repository.goods;

import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface GoodsIssueRepository
        extends JpaRepository<GoodsIssue, Long> {

    long countByIssueDateBetween(
            LocalDate start,
            LocalDate end
    );

    @Query("""
            SELECT YEAR(g.issueDate),
                   MONTH(g.issueDate),
                   COUNT(g)
            FROM GoodsIssue g
            GROUP BY YEAR(g.issueDate), MONTH(g.issueDate)
            ORDER BY YEAR(g.issueDate), MONTH(g.issueDate)
            """)
    List<Object[]> monthlyIssueStatistics();

    @Query("""
            SELECT COUNT(i)
            FROM GoodsIssue i
            WHERE i.status = :status
            """)
    long countByStatus(

            @Param("status")
            IssueStatus status

    );
}
