package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.MaterialInventory;
import com.github.xnaut97.wms.entity.material.Material;
import com.github.xnaut97.wms.repository.MaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.MaterialInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class InventorySeeder {

    private final MaterialInventoryRepository materialInventoryRepository;

    private final WarehouseRepository warehouseRepository;

    private final MaterialRepository materialRepository;

    @Transactional
    public void seed() {
        if (materialRepository.count() == 0) return;
        if (warehouseRepository.count() == 0) return;
        if (materialInventoryRepository.count() > 0) return;

        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<Material> materials = materialRepository.findAll();

        for (Warehouse warehouse : warehouses) {
            for (Material material : materials) {
                if (MaterialSeeder.SCENARIO_MATERIAL_CODES
                        .contains(material.getCode())) {
                    continue;
                }

                MaterialInventory materialInventory = new MaterialInventory();
                materialInventory.setWarehouse(warehouse);
                materialInventory.setMaterial(material);
                materialInventory.setQuantity(randomQuantity());
                materialInventoryRepository.save(materialInventory);
            }

        }

        System.out.println("✓ Inventory seeded");

    }

    private BigDecimal randomQuantity() {

        int quantity = ThreadLocalRandom.current().nextInt(80, 600);

        return BigDecimal.valueOf(quantity);

    }

}