package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.receipt.ReceiptDailyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptMonthlyReportResponse;
import com.github.xnaut97.wms.dto.report.receipt.ReceiptSupplierReportResponse;
import com.github.xnaut97.wms.enums.ReceiptStatus;
import com.github.xnaut97.wms.repository.report.ReceiptReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReceiptReportService {

    private final ReceiptReportRepository receiptReportRepository;

    public ReceiptDailyReportResponse getDailyReceiptReport(LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        return receiptReportRepository.getDailyReport(
                date,
                ReceiptStatus.CONFIRMED
        );
    }

    public List<ReceiptMonthlyReportResponse> getMonthlyReceiptReport(int year) {

        return receiptReportRepository.getMonthlyReport(
                year,
                ReceiptStatus.CONFIRMED
        );
    }

    public List<ReceiptSupplierReportResponse> getSupplierReceiptReport(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return receiptReportRepository.getSupplierReport(
                fromDate,
                toDate,
                ReceiptStatus.CONFIRMED
        );
    }

}