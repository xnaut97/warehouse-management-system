package com.github.xnaut97.wms.seed;

import com.github.xnaut97.wms.entity.common.Warehouse;
import com.github.xnaut97.wms.entity.inventory.Inventory;
import com.github.xnaut97.wms.entity.material.RawMaterial;
import com.github.xnaut97.wms.repository.RawMaterialRepository;
import com.github.xnaut97.wms.repository.WarehouseRepository;
import com.github.xnaut97.wms.repository.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class InventorySeeder {

    private final InventoryRepository inventoryRepository;

    private final WarehouseRepository warehouseRepository;

    private final RawMaterialRepository materialRepository;

    @Transactional
    public void seed() {
        if (materialRepository.count() == 0) return;
        if (warehouseRepository.count() == 0) return;
        if (inventoryRepository.count() > 0) return;

        List<Warehouse> warehouses = warehouseRepository.findAll();
        List<RawMaterial> materials = materialRepository.findAll();

        for (Warehouse warehouse : warehouses) {
            for (RawMaterial material : materials) {
                Inventory inventory = new Inventory();
                inventory.setWarehouse(warehouse);
                inventory.setMaterial(material);
                inventory.setQuantity(randomQuantity());
                inventoryRepository.save(inventory);
            }

        }

        System.out.println("✓ Inventory seeded");

    }

    private BigDecimal randomQuantity() {

        int quantity = ThreadLocalRandom.current().nextInt(80, 600);

        return BigDecimal.valueOf(quantity);

    }

}