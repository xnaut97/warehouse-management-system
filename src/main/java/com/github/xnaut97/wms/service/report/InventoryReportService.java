package com.github.xnaut97.wms.service.report;

import com.github.xnaut97.wms.dto.report.inventory.InventoryHistoryResponse;
import com.github.xnaut97.wms.dto.report.inventory.InventoryReportResponse;
import com.github.xnaut97.wms.repository.report.InventoryReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryReportService {

    private final InventoryReportRepository inventoryReportRepository;

    @Transactional
    public List<InventoryReportResponse> getRawMaterialInventoryReport() {

        return inventoryReportRepository.getRawMaterialInventory();

    }

    @Transactional
    public List<InventoryHistoryResponse> getInventoryHistoryReport(
            Long warehouseId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return inventoryReportRepository.getHistory(

                warehouseId,

                fromDate == null ? null : fromDate.atStartOfDay(),

                toDate == null ? null : toDate.atTime(23,59,59)

        );

    }
}
