package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.stocktaking.SaveStocktakingCountRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingCountLineRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingDetailResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingItemBatchResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingItemResponse;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.enums.StocktakingStatus;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StocktakingVarianceSeeder {

    private static final BigDecimal VARIANCE_STEP = new BigDecimal("5");

    private static final BigDecimal VARIANCE_THRESHOLD = new BigDecimal("10");

    private static final int MAX_VARIANCE_LINES = 3;

    private final StocktakingService stocktakingService;

    private final StocktakingRepository stocktakingRepository;

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public void seed() {

        seedMaterialVariance();

        seedProductVariance();

        System.out.println("✓ Stocktaking variance scenario seeded");

    }

    private void seedMaterialVariance() {

        Optional<Warehouse> warehouse =
                warehouseRepository.findByCode(
                        WarehouseService.MATERIAL_WAREHOUSE_CODE
                );

        if (warehouse.isEmpty()) return;

        if (alreadySeeded(warehouse.get())) return;

        Long stocktakingId = create(
                warehouse.get(),
                "Kiểm kê đột xuất nguyên vật liệu"
        );

        if (stocktakingId == null) return;

        StocktakingDetailResponse detail =
                stocktakingService.getById(stocktakingId);

        List<StocktakingCountLineRequest> lines = new ArrayList<>();

        int varianceLines = 0;

        for (StocktakingItemResponse item : detail.getItems()) {

            boolean applyVariance =
                    varianceLines < MAX_VARIANCE_LINES
                            && item.getSystemQuantity()
                            .compareTo(VARIANCE_THRESHOLD) >= 0;

            if (applyVariance) {
                varianceLines++;
            }

            lines.add(
                    line(
                            item.getId(),
                            applyVariance
                                    ? item.getSystemQuantity()
                                    .subtract(VARIANCE_STEP)
                                    : item.getSystemQuantity(),
                            applyVariance
                                    ? "Chênh lệch phát hiện khi kiểm kê"
                                    : null
                    )
            );

        }

        if (lines.isEmpty()) return;

        SaveStocktakingCountRequest request =
                new SaveStocktakingCountRequest();

        request.setItems(lines);

        stocktakingService.confirm(stocktakingId, request);

    }

    private void seedProductVariance() {

        Optional<Warehouse> warehouse =
                warehouseRepository.findByCode(
                        WarehouseService.PRODUCT_WAREHOUSE_CODE
                );

        if (warehouse.isEmpty()) return;

        if (alreadySeeded(warehouse.get())) return;

        Long stocktakingId = create(
                warehouse.get(),
                "Kiểm kê định kỳ kho sản phẩm"
        );

        if (stocktakingId == null) return;

        StocktakingDetailResponse detail =
                stocktakingService.getById(stocktakingId);

        List<StocktakingCountLineRequest> batchLines = new ArrayList<>();

        int varianceLines = 0;

        for (StocktakingItemResponse item : detail.getItems()) {

            if (item.getBatches() == null) continue;

            for (StocktakingItemBatchResponse batch : item.getBatches()) {

                boolean applyVariance =
                        varianceLines < MAX_VARIANCE_LINES
                                && batch.getSystemQuantity()
                                .compareTo(VARIANCE_THRESHOLD) >= 0;

                if (applyVariance) {
                    varianceLines++;
                }

                batchLines.add(
                        line(
                                batch.getId(),
                                applyVariance
                                        ? batch.getSystemQuantity()
                                        .subtract(VARIANCE_STEP)
                                        : batch.getSystemQuantity(),
                                applyVariance
                                        ? "Chênh lệch lô hàng khi kiểm kê"
                                        : null
                        )
                );

            }

        }

        if (batchLines.isEmpty()) return;

        SaveStocktakingCountRequest request =
                new SaveStocktakingCountRequest();

        request.setBatches(batchLines);

        stocktakingService.confirm(stocktakingId, request);

    }

    private boolean alreadySeeded(Warehouse warehouse) {

        return stocktakingRepository.existsByWarehouseIdAndStatus(
                warehouse.getId(),
                StocktakingStatus.COUNT_CONFIRMED
        );

    }

    private Long create(
            Warehouse warehouse,
            String note
    ) {

        StocktakingRequest request = new StocktakingRequest();

        request.setWarehouseId(warehouse.getId());

        request.setStocktakingDate(LocalDate.now());

        request.setType(StocktakingType.AD_HOC);

        request.setNote(note);

        return stocktakingService.create(request).getId();

    }

    private StocktakingCountLineRequest line(
            Long id,
            BigDecimal physicalQuantity,
            String reason
    ) {

        StocktakingCountLineRequest line =
                new StocktakingCountLineRequest();

        line.setId(id);

        line.setPhysicalQuantity(physicalQuantity);

        line.setReason(reason);

        return line;

    }

}
