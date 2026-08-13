package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.stocktaking.AddStocktakingItemRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.enums.StocktakingType;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import com.github.xnaut97.wms.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Transactional
public class StocktakingSeeder {

    private final StocktakingService stocktakingService;
    private final WarehouseRepository warehouseRepository;
    private final MaterialRepository materialRepository;

    public void seed() {

        if (materialRepository.count() == 0) return;

        Optional<Warehouse> materialWarehouse =
                warehouseRepository.findByCode(
                        WarehouseService.MATERIAL_WAREHOUSE_CODE
                );

        if (materialWarehouse.isEmpty()) return;

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        List<Material> materials = materialRepository.findAll();

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

            int randomItems = random.nextInt(1, materials.size() + 1);

            for (int j = 0; j < randomItems; j++) {
                Material material = materials.get(j);
                AddStocktakingItemRequest addItemRequest = new AddStocktakingItemRequest();

                addItemRequest.setMaterialId(material.getId());
                addItemRequest.setPhysicalQuantity(new BigDecimal(random.nextInt(100, 1000)));

                stocktakingService.addItem(stocktakingId, addItemRequest);
            }

            stocktakingService.confirm(stocktakingId);

            stocktakingService.balance(stocktakingId);

        }

        System.out.println("✓ Stocktaking Seeder Completed");

    }

}
