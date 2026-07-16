package com.github.xnaut97.wms.repository.report;

import com.github.xnaut97.wms.dto.report.issue.IssueCustomerReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueDailyReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueMaterialReportResponse;
import com.github.xnaut97.wms.entity.goods.GoodsIssue;
import com.github.xnaut97.wms.enums.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IssueReportRepository
        extends JpaRepository<GoodsIssue, Long> {

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.issue.IssueDailyReportResponse(
            :date,
            COUNT(DISTINCT g.id),
            COALESCE(SUM(i.quantity),0),
            COALESCE(SUM(i.amount),0)
        )
        FROM GoodsIssue g
        LEFT JOIN GoodsIssueItem i
            ON i.issue = g
        WHERE g.status = :status
        AND g.issueDate = :date
        """)
    IssueDailyReportResponse getDailyReport(
            @Param("date") LocalDate date,
            @Param("status") IssueStatus status
    );

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.issue.IssueCustomerReportResponse(
            c.id,
            c.name,
            COUNT(DISTINCT g.id),
            COALESCE(SUM(i.quantity),0),
            COALESCE(SUM(i.amount),0)
        )
        FROM GoodsIssue g
        JOIN g.customer c
        LEFT JOIN GoodsIssueItem i
            ON i.issue = g
        WHERE g.status = :status
        AND g.issueDate BETWEEN :fromDate AND :toDate
        GROUP BY c.id,c.name
        ORDER BY c.name
        """)
    List<IssueCustomerReportResponse> getCustomerReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") IssueStatus status
    );

    @Query("""
        SELECT new com.github.xnaut97.wms.dto.report.issue.IssueMaterialReportResponse(
            m.id,
            m.code,
            m.name,
            COALESCE(SUM(i.quantity),0),
            COALESCE(SUM(i.amount),0)
        )
        FROM GoodsIssueItem i
        JOIN i.material m
        JOIN i.issue g
        WHERE g.status = :status
        AND g.issueDate BETWEEN :fromDate AND :toDate
        GROUP BY m.id,m.code,m.name
        ORDER BY m.code
        """)
    List<IssueMaterialReportResponse> getMaterialReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("status") IssueStatus status
    );

}