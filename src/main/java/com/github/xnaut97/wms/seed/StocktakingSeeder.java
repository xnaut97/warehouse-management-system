package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.stocktaking.SaveStocktakingCountRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingCountLineRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.stocktaking.StocktakingRepository;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Transactional
public class StocktakingSeeder {

    private final StocktakingService stocktakingService;
    private final StocktakingRepository stocktakingRepository;
    private final WarehouseRepository warehouseRepository;
    private final MaterialRepository materialRepository;

    public void seed() {

        if (materialRepository.count() == 0) return;

        Optional<Warehouse> materialWarehouse =
                warehouseRepository.findByCode(
                        WarehouseService.MATERIAL_WAREHOUSE_CODE
                );

        if (materialWarehouse.isEmpty()) return;

        if (stocktakingRepository.existsByWarehouseId(
                materialWarehouse.get().getId()
        )) {
            return;
        }

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        for (int i = 0; i < 3; i++) {

            StocktakingRequest request =
                    new StocktakingRequest();

            request.setWarehouseId(

                    materialWarehouse.get().getId()

            );

            request.setStocktakingDate(

                    LocalDate.now()

                            .minusDays(

                                    random.nextInt(30)

                            )

            );

            request.setType(
                    StocktakingType.PERIODIC
            );

            request.setNote(
                    "Monthly inventory counting"
            );

            Long stocktakingId = stocktakingService.create(request).getId();

            SaveStocktakingCountRequest countRequest =
                    new SaveStocktakingCountRequest();

            countRequest.setItems(
                    stocktakingService.getById(stocktakingId)
                            .getItems()
                            .stream()
                            .map(item -> {

                                StocktakingCountLineRequest line =
                                        new StocktakingCountLineRequest();

                                line.setId(item.getId());

                                line.setPhysicalQuantity(
                                        new BigDecimal(
                                                random.nextInt(100, 1000)
                                        )
                                );

                                return line;

                            })
                            .toList()
            );

            if (countRequest.getItems().isEmpty()) {
                continue;
            }

            stocktakingService.confirm(stocktakingId, countRequest);

            stocktakingService.balance(stocktakingId);

        }

        System.out.println("✓ Stocktaking Seeder Completed");

    }

}
