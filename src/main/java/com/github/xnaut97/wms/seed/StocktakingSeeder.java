package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.dto.stocktaking.AddStocktakingItemRequest;
import com.github.xnaut97.wms.dto.stocktaking.StocktakingRequest;
import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.repository.RawMaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.service.stock.StocktakingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Transactional
public class StocktakingSeeder {

    private final StocktakingService stocktakingService;
    private final WarehouseRepository warehouseRepository;
    private final RawMaterialRepository materialRepository;

    public void seed() {

        if (materialRepository.count() == 0) return;

        ThreadLocalRandom random =
                ThreadLocalRandom.current();

        List<Warehouse> warehouses =
                warehouseRepository.findAll();

        List<RawMaterial> materials = materialRepository.findAll();

        for (int i = 0; i < 3; i++) {

            StocktakingRequest request =
                    new StocktakingRequest();

            request.setWarehouseId(

                    warehouses.get(

                            random.nextInt(

                                    warehouses.size()

                            )

                    ).getId()

            );

            request.setStocktakingDate(

                    LocalDate.now()

                            .minusDays(

                                    random.nextInt(30)

                            )

            );

            request.setNote(
                    "Monthly inventory counting"
            );

            Long stocktakingId = stocktakingService.create(request).getId();

            int randomItems = random.nextInt(1, materials.size() + 1);

            for (int j = 0; j < randomItems; j++) {
                RawMaterial material = materials.get(j);
                AddStocktakingItemRequest addItemRequest = new AddStocktakingItemRequest();

                addItemRequest.setMaterialId(material.getId());
                addItemRequest.setPhysicalQuantity(new BigDecimal(random.nextInt(100, 1000)));

                stocktakingService.addItem(stocktakingId, addItemRequest);
            }

            stocktakingService.confirm(stocktakingId);

        }

        System.out.println("✓ Stocktaking Seeder Completed");

    }

}