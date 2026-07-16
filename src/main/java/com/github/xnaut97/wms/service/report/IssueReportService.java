package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.issue.IssueCustomerReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueDailyReportResponse;
import com.github.xnaut97.wms.dto.report.issue.IssueMaterialReportResponse;
import com.github.xnaut97.wms.enums.IssueStatus;
import com.github.xnaut97.wms.repository.report.IssueReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueReportService {

    private final IssueReportRepository issueReportRepository;

    public IssueDailyReportResponse getDailyIssueReport(
            LocalDate date
    ) {

        if (date == null) {
            date = LocalDate.now();
        }

        return issueReportRepository.getDailyReport(
                date,
                IssueStatus.CONFIRMED
        );

    }

    public List<IssueCustomerReportResponse> getCustomerIssueReport(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return issueReportRepository.getCustomerReport(
                fromDate,
                toDate,
                IssueStatus.CONFIRMED
        );

    }

    public List<IssueMaterialReportResponse> getMaterialIssueReport(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return issueReportRepository.getMaterialReport(
                fromDate,
                toDate,
                IssueStatus.CONFIRMED
        );

    }
}
